<h1 align="center">
Smart Alert Android App
</h1>

<p align="center">Android Studio - Firebase<p>

## Overview

This applications notifies the user when an earthquake occurs. Also, it sends a text message to close contacts on fall detection of the user.

## Features

###### When the device is charging (Earthquake detection mode)
The app uses the device accelerometer to detect earthquakes. Before notifying the user with a sound, it makes a validation check to ensure that is a real earthquake. This is done by using data from  other close (5km distance) users. If a a close user has an earthquake status the same time (-/+3 seconds) then notifies the user with a sound and a message.

###### When the device is not charging and the app is open (Fall detection mode)
Sends a text message to close contacts when a fall of the user is detected. The user can stop the alarm if it is accidentally enabled.

###### On both states
- The user can press the sos button to send an "SOS" text message to contacts.
- Activity and statistics
- Abort button which stops the timers or sends a text message to close contacts informing them that is a false alarm.
- Contacts page to change the close contacts (max 3 numbers)

## Installation

> You need to have Android Studio installed to run the project locally

 1) Download or clone from github
 2) Open the project from android studio and wait until gradle finish
 3) Setup your own [firebase](https://console.firebase.google.com/) project and import the exported json with configurations into the project as shown in the firebase new project tutorial.
    You can also use the Firebase Assistant within Android studio to easily connect your project with firebase Tools> Firebase> Realtime Database > Connect to Firebase
    (You cannot build the app without the firebase SDK)
 4) Build and run the apk on real device
 
 You can preview the app by downloading a debug apk from here [smart-alert](https://drive.google.com/open?id=10zjtTQSylPVqjJr-1mUfOXbydgKij-oF)
 

## Development
Technologies:
- Android Studio
- Firebase

## Screenshots

![mobile](https://user-images.githubusercontent.com/3619970/74785349-cf12e600-52b2-11ea-8296-12e478603638.gif)




