![Jumbotron Logo](https://cloud.githubusercontent.com/assets/3138467/11771083/7c0e4b86-a1d9-11e5-9719-7c68c665395a.png)
--------------------------------

Jumbotron is (will be) a tool for tracking and displaying the scores of a game. Once
tracked, scores will be stored on the device via the parse lib and an option will be
given to sync them to parse.com. A big feature of this application is the ability to 
display scores via Chromecast. Schools could then use projectors or TVs equiped with a 
Chromecast Dongle to have a low-buget Jumbotron. 

## Goals
 - Never require an active internet connection for core functionality
 - Display active game scores on to paired Android Devices
   - TVs (Chromecast)
   - Android Wearables
   - Android Phones
   - Android Tablets
 - Allow paired devices to contribute 
   - Timeout period to avoid two people trying to log the same score?

## Prototypes
Prototypes for this app can be located at the following URL. Hold "Shift" and clickable
areas will be highlighted green.

https://app.moqups.com/blackthorns.legacy@gmail.com/6Dtms3wF9b/view/page/a32231f77

## Legal 
JumboTron is a registered trademark owned by the Sony Corporation. JumboTron 
is now a genericized trademark and Sony has not manufactured such a device since 2001.

## Build Requirements
You need to have `ParseAppId` and `ParseClientKey` defined. I put this into `%userprofile%\.gradle\gradle.properties`:
```
ParseAppId=AsDf<APP ID>GhJk
ParseClientKey=qwEE<CLIENT_KEY>fluCF
```
