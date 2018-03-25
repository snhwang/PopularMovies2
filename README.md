# Popular Movies (Stage 1)

Scott Hwang

snhwang@alum.mit.edu

## Intro

Popular Movies was created in 2 stages for the first and second projects for the Udacity Android Developer Nanodegree course. It accesses The Movie Database (TMDb, https://www.themoviedb.org) and displays information about the movies from either the list of the most popular or the list of the top rated. The user can also choose to display a list of movies that were favorited.

## About the App

The app was developed using Android Studio 3.0.1. API 15: Android 4.0.3 (Ice Cream Sandwich) was set as the minimum for the SDK. It was tested on an emulator for Nexus 5x with APIs of 24 and 26. The only testing on a real phone was with a Samsung S8+ (SM-G955U) running Android version 7.0 (API 24).

Before building/running, please place your TMDb API key in the gradle.properties file in the main folder:

```
API_KEY = "your-api-key"
```

Otherwise, the app will fail. The gradle.properties file is included in .gitignore so that it will not be uploaded to github.

The following libraries were included as shown from this excerpt from build.gradle (Modules:App):

```
    implementation 'com.squareup.picasso:picasso:2.5.2'
    implementation 'com.squareup.okhttp3:okhttp:3.9.1'
    implementation 'com.squareup.okio:okio:1.13.0'
    implementation 'com.android.support:recyclerview-v7:26.1.0'
    implementation 'android.arch.persistence.room:runtime:1.0.0'

```

Picasso is a library with functions for displaying images on the basis of urls. The library okhttp was useful for downloading data from TBDb. The library okio is required by okhttp. I used the room persistence library to facilitate database operations. A content provider was also implemented to potentially allow access from other apps although no specific external applications have been identified at the this time for this functionality.

I found assistance from multiple sources, including the Udacity course material and stackoverflow. I commented in the code at the sites where I relied most heavily on outside code. If I forgot to give credit for any significant adaptation of code, please let me know.

I initially used onSaveInstanceState() and onCreate() to save the state of the main activity during orientation changes. However, it did not save the state when changing activities, i.e. from the main activity to the movie detail activity and back to the main activity. To solve this problem, I stored the state of the movie list in preferences using onPause() and onResume(). This appears to maintain the state during orientation changes and activity changes as well as redisplaying the same list after closing and re-running the app. The Detail activity for displaying the movie details still relies on onSaveInstanceState and onRestoreInstanceState to maintain state during device orientation changes. Both the list of trailers and reviews are maintained in the savedInstanceState, including whether the review content or just the review title is shown for each review.

## Instructions

I recommend using the app in portrait mode. Although the app works in landscape orientation, the aspect ratio of the poster images from TBDb is better suited for the portrait rather than the landscape display. The home display is a grid of pics of of the movie posters. A pop up selector at the top enables the user to choose which list of movies to display in the grid. The display is automatically updated. "Prev" and "Next" Buttons at the top enable the user to move to the previous or next 20 movies in the list. The "First" button returns the list back to the first page. If the favorites are displayed, pressing the page buttons has no effect. 

Pressing on a poster in the Main Activity switches to the Details Activity, which displays details about the movie, including the title, release date, average vote score, and a synopsis. Pressing the back arrow of the device or the back arrow in the action bar returns the user to the home page. In the movie details page, the user can click on the star to indicate that the movie is a favorite. The database of favorited movies is immediately updated.

The details page also displays a list of trailer/video titles. Clicking on one of them plays the video in the YouTube application if available or in a browser.

A lists of reviews is also shown. Initially, only the author names are shown. Clicking on an author displays the content of the review. Clicking on the author or content again hides the content.

When the app is closed then re-opened, the app will be in the Main Activity. The most recent list (Popular vs Top Rated vs Favorites) will be shown at the last viewed page.