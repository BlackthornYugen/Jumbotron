package com.steelcomputers.android.jumbotron;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.util.Objects;

/**
 * This service handles communications between the app and a cast device. It was
 * sourced from github.com/BlackthornYugen/CastHelloText-android, a fork of
 * googlecast/CastHelloText-android
 */
public class CastScoreService extends Service implements Contestant.GameListener {
    private static final String TAG = CastScoreService.class.getSimpleName();

    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private HelloWorldChannel mHelloWorldChannel;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private String mSessionId;
    private Contestant mAway;
    private Contestant mHome;

    public CastScoreService() {
        Log.d(TAG, "Started");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ScoreBinder();
    }

    /**
     * Set the score of a player
     * @param name the player's name
     * @param score the player's score
     */
    @Override
    public void score(String name, int score) {
        String playerName;
        boolean isHomePlayer = Objects.equals(mHome.getName(), name);
        int playerIndex = isHomePlayer ? 1 : 2;
        if (isHomePlayer) {
            playerName = mHome.getName(true);
        } else {
            playerName = mAway.getName(true);
        }
        sendMessage(String.format("name/%d/%s", playerIndex, playerName));
        sendMessage(String.format("score/%d/%s", playerIndex, score));
    }

    public class ScoreBinder extends Binder {
        /**
         * Send a raw message to the chromecast
         * @param message the message to send
         */
        public void sendMessage(String message) {
            Log.d(TAG, "sendMessage: " + message);
            CastScoreService.this.sendMessage(message);
        }

        /**
         * Launch the receiver if not running
         * @param device the cast device to use
         */
        public void launchReceiver(CastDevice device) {
            launchReceiver(device, false);
        }

        /**
         * Launch the receiver and replace one if it exists.
         * @param device the case device to use
         * @param replace true to replace existing device
         */
        public void launchReceiver(CastDevice device, boolean replace) {
            if (mSelectedDevice == null || replace) {
                Log.d(TAG, "launchReceiver");
                mSelectedDevice = device;
                CastScoreService.this.launchReceiver();
            }
        }

        /**
         * Shutdown link to chromecast
         */
        public void teardown() {
            CastScoreService.this.teardown();
        }

        public void watchGame(Contestant home, Contestant away) {
            if (mHome != null && mAway != null) { // Remove old listeners
                Contestant.removeListener(CastScoreService.this, away, home);
                Contestant.removeListener(CastScoreService.this, home, away);
            }

            // Add new listeners
            Contestant.addListener(CastScoreService.this, home, away);
            Contestant.addListener(CastScoreService.this, away, home);

            // Store player references, TODO: Consider weak refs
            mHome = home;
            mAway = away;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        teardown();
        super.onDestroy();
    }

    /**
     * Start the receiver app
     */
    void launchReceiver() {
        try {
            Cast.Listener mCastListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    teardown();
                }

            };
            // Connect to Google Play services
            ConnectionCallbacks mConnectionCallbacks = new ConnectionCallbacks();
            ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(mSelectedDevice, mCastListener);
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();

            mApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected");

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null)
                            && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d(TAG, "App  is no longer running");
                        teardown();
                    } else {
                        // Re-create the custom message channel
                        try {
                            Cast.CastApi.setMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace(),
                                    mHelloWorldChannel);
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while creating channel", e);
                        }
                    }
                } else {
                    // Launch the receiver app
                    Cast.CastApi.launchApplication(mApiClient, getString(R.string.app_id), false)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                Cast.ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            Log.d(TAG,
                                                    "ApplicationConnectionResultCallback.onResult:"
                                                            + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();
                                                mSessionId = result.getSessionId();
                                                String applicationStatus = result
                                                        .getApplicationStatus();
                                                boolean wasLaunched = result.getWasLaunched();
                                                Log.d(TAG, "application name: "
                                                        + applicationMetadata.getName()
                                                        + ", status: " + applicationStatus
                                                        + ", sessionId: " + mSessionId
                                                        + ", wasLaunched: " + wasLaunched);
                                                mApplicationStarted = true;

                                                // Create the custom message
                                                // channel
                                                mHelloWorldChannel = new HelloWorldChannel();
                                                try {
                                                    Cast.CastApi.setMessageReceivedCallbacks(
                                                            mApiClient,
                                                            mHelloWorldChannel.getNamespace(),
                                                            mHelloWorldChannel);
                                                } catch (IOException e) {
                                                    Log.e(TAG, "Exception while creating channel",
                                                            e);
                                                }

                                                // set the initial instructions
                                                // on the receiver
                                                sendMessage("App Connected");

                                                // Ask for an update
                                                mHome.notifyGameListeners(mAway);
                                                mAway.notifyGameListeners(mHome);
                                            } else {
                                                Log.e(TAG, "application could not launch");
                                                teardown();
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended");
            mWaitingForReconnect = true;
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed ");

            teardown();
        }
    }

    /**
     * Tear down the connection to the receiver
     */
    private void teardown() {
        Log.d(TAG, "teardown");
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected() || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mHelloWorldChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace());
                            mHelloWorldChannel = null;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        mSessionId = null;
    }

    /**
     * Send a text message to the receiver
     * @param message The raw message body to send to te cast device
     */
    private void sendMessage(String message) {
        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient,
                        mHelloWorldChannel.getNamespace(), message).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status result) {
                                if (!result.isSuccess()) {
                                    Log.e(TAG, "Sending message failed");
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        }
    }

    /**
     * Custom message channel
     */
    class HelloWorldChannel implements Cast.MessageReceivedCallback {

        /**
         * @return custom namespace
         */
        public String getNamespace() {
            return getString(R.string.namespace);
        }

        /**
         * Receive message from the receiver app
         * @param castDevice The cast device the message was recived from
         * @param namespace The namespace used to transmit the message
         * @param message The message body
         */
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
                                      String message) {
            Log.d(TAG, "onMessageReceived: " + message);
        }

    }

}