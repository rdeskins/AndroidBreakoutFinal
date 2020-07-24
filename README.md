# AndroidBreakoutFinal
An Android app written for my final project in CSCD-372 Android Programming at Eastern Washington University in Spring 2020. Android Studio was used to make this project.

## Getting Started
I tested this app on a GenyMotion emulator which can be downloaded [here](https://www.genymotion.com/download/). I believe at least Api 19 is needed to run this. From there, dragging and dropping the apk file into the emulator will install the app. 

## Features
The game begins in a paused state and can be paused or unpaused by pressing the play area. The game can only be played in landscape orientation. The paddle at the bottom can be moved left and right by pressing and holding the respective blue buttons on either side of the screen. The paddle can still be moved in the paused state, this has been left in to make the game easier to play and test.
![alt text](https://github.com/rdeskins/AndroidBreakoutFinal/blob/master/screenshots/intial_pause.png)

When a block is hit, it either changes color or is destroyed, depending on how many hits it has left. When all blocks are hit, the player advances to the next level. The speed of the ball is also increased by 33%. 
![alt text](https://github.com/rdeskins/AndroidBreakoutFinal/blob/master/screenshots/play.png)

When the ball hits the bottom of the screen, a ball is lost. If the player's ball count reaches 0, they game is reset to level 1 and a game over message is displayed. 
![alt text](https://github.com/rdeskins/AndroidBreakoutFinal/blob/master/screenshots/game_over.png)

The settings option in the menu will open a preferences screen.
![alt text](https://github.com/rdeskins/AndroidBreakoutFinal/blob/master/screenshots/initial_settings.png)

Another screenshot showing changed settings.
![alt text](https://github.com/rdeskins/AndroidBreakoutFinal/blob/master/screenshots/another_screenshot.png)
