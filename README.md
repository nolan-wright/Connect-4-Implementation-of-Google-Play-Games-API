# Red vs Green - a Connect 4 game that implements the Google Play Games API <sub><sup>version 1.0</sup></sub>

## What the project does

This project surveys the capabilities of the Google Play Games API for Android. Specifically, it implements [Real-time Multiplayer Support](https://developers.google.com/games/services/android/realtimeMultiplayer) for a Connect 4 game. This project also implements [achievements](https://developers.google.com/games/services/android/achievements), a [leaderboard](https://developers.google.com/games/services/android/leaderboards) made possible with a [Firebase Database](https://firebase.google.com/docs/database/), and [Google authentication](https://developers.google.com/games/services/android/signin).


## Why the project is useful

This project is useful as an example of how the Google Play Games API can be implemented in a simple game.


## How users can get started with the project

There are number of steps that must be completed before one can get started with the Google Play Games API. Consult [Google's documentation](https://developers.google.com/games/services/android/quickstart) for more details.

The below problem points stand out to me:
* Ensure you have the necessary SDKs installed. In Android Studio, this can be accomplished through File > Settings.
* Ensure you are running a suitable version of Android. The Android Emulators I used during testing ran an Oreo system image (API Level 26) with Google APIs. The Google APIs are required to enable testing with Google Play Services.
* Ensure you have added test accounts to use while testing the application. In the Google Play Console, this can be accomplished through Game services > *Your game* > Testing > ADD TESTERS.
* To test the application, ensure you have configured OAuth2 authorization to work with the debug variant of your application. In the Google Play Console, this can be accomplished by linking the debug variant as a separate app in Game services > *Your game* > Linked apps > Link another app. Provide the debug variant's SHA when prompted to configure OAuth2 authorization.


## Where users can get help with the project

If difficulties are encountered either in running this project or in one's own project, see [the above section](#how-users-can-get-started-with-the-project). If that does not help, I advise making use of [Google's documentation](https://developers.google.com/games/services/android/quickstart) and [Stack Overflow](https://stackoverflow.com/).


## Who maintains and contributes to the project

This project was created by Nolan Wright. I can be reached by email at wiljawright@gmail.com. No further work is planned for this project.

## Acknowledgements

No acknowledgments


## License

MIT License

Copyright (c) 2017 Nolan Wright

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
