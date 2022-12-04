# DocumentScanner
Simple android app created using OpenCv library to scan documents

# Table of contents
* [Setup](#setup)
* [Technologies](#technologies)
* [Dependencies](#dependencies)
* [Permission](#permission)

# Setup
- Fork the repo to your Github account
- Install and setup NDK to work with OpenCv
- Set NDK and SDK path in local.properties
- Setup Firebase project with your gmail id and add this app
- Enable Firebase authentication with Phone Number login
- Hurray Setup complete, now you can test our app

# Technologies
- Google Firebase 
  - Firebase mobile authentication to register verified users.
  - Firebase Crashanalytics to detect crashes in app modules.


# Dependencies
- OpenCv Sdk modules
- View Model
  - androidx.lifecycle:lifecycle-extensions
- LiveData
  - androidx.lifecycle:lifecycle-livedata
- Room database
  - androidx.room:room-runtime
- Itext image2pdf convertor
  - com.itextpdf:itextg
- Country code picker
  - com.hbb20:ccp
- Image Filter
  - info.androidhive:imagefilters
- Firebase
  - firebase-analytics
  - firebase-auth
  - firebase-crashlytics
- Gson dependency
  - com.squareup.retrofit2:converter-gson
- Image cropping API
  - com.theartofdev.edmodo:android-image-cropper

# Permissions
- Internet permission
  - To access advertisement in smartAdvertisement and authentication, saving records in diseaseDetection
- External Storage
  - To access media files inside the mobile phone
- Camera
  - To capture photo 
