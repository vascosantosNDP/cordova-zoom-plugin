# Zoom Outsystems Software Development Kit (SDK)

<div align="center">
<img src="https://s3.amazonaws.com/user-content.stoplight.io/8987/1541013063688" width="400px" max-height="400px" style="margin:auto;"/>
</div>

## Table of Contents
- [:rotating_light: Announcement :rotating_light:](#rotating_light-announcement-rotating_light)   
- [Prerequisites](#prerequisites)   
- [Installing](#installing)   
- [Usage](#usage)   
- [License](#license)


## :rotating_light: Announcement :rotating_light:
To align with Zoom’s [recent announcement](https://blog.zoom.us/wordpress/2020/04/22/zoom-hits-milestone-on-90-day-security-plan-releases-zoom-5-0/) pertaining to our security initiative, Zoom Client SDKs have added **AES 256-bit GCM encryption** support, which provides more protection for meeting data and greater resistance to tampering. **The system-wide account enablement of AES 256-bit GCM encryption will take place on June 01, 2020.** You are **strongly recommended** to start the required upgrade to this latest version 4.6.21666.0512 at your earliest convenience. Please note that any Client SDK versions below 4.6.21666.0512 will **no longer be operational** from June 01.

> If you would like to test the latest SDK with AES 256-bit GCM encryption meeting before 05/30, you may:
> 1. Download the latest version of Zoom client: https://zoom.us/download
> 2. Visit https://zoom.us/testgcm and launch a GCM enabled meeting with your Zoom client, you will see a Green Shield icon that indicates the GCM encryption is enabled
> 3. Use SDK to join this meeting

## Prerequisites

Before you try out our SDK, you would need the following to get started:

* **A Zoom Account**: If you do not have one, you can sign up at [https://zoom.us/signup](https://zoom.us/signup).
  * Once you have your Zoom Account, sign up for a 60-days free trial at [https://marketplace.zoom.us/](https://marketplace.zoom.us/)
* **A mobile device**
  * Android
    * Android 5.0 (API Level 21) or later.
    * CPU: armeabi-v7a, x86, armeabi, arm64-v8a, x86_64
    * **compileSdkVersion**: 29+
    * **buildToolsVersion**: 29+
    * **minSdkVersion**: 21
    * **Required dependencies**
    ```
    implementation 'androidx.multidex:multidex:2.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.0.0'
    implementation 'androidx.appcompat:appcompat:1.0.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    implementation 'com.google.android.material:material:1.0.0-rc01'
    ```
  * iOS
    * iPhone or iPad
    * **npm@6.7.0+**
    * **cordova-cli@7.1.0+**
    

  
 If you are developing on Android, you will need to install the 8.0.0 version of cordova-android
 ```
 cordova platform add android@8.0.0
 ```
  If you are developing on iOS, you will need to install the 5.1.0 version of cordova-ios
 ```
 cordova platform add ios@5.1.0
 ```

## Installing
Local:
  Clone or download a copy of our SDK files from GitHub. After you unzipped the file, you should have the following folders:

  ```
  .
  ├── README.md
  ├── libs
  ├── package.json
  ├── plugin.xml
  ├── src
  └── www
  ```
  In your cordova application directory, run the following to install the plugin:
  ```
  cordova plugin add cordova.plugin.zoom --variable IOS_CAMERA_USAGE_DESCRIPTION="Usage description" --variable IOS_MICROPHONE_USAGE_DESCRIPTION="Usage description"
  ```
Outsystems:
  In the extensibility configurations of the plugin wrapper put:
  {
    ,
    variables:[{"key":"IOS_CAMERA_USAGE_DESCRIPTION","value":"Usage description"},{"key":"IOS_MICROPHONE_USAGE_DESCRIPTION","value":"Usage description"}]
  }

## Usage

1. Initialize Zoom SDK
Initialize Zoom SDK, need to be called when app fired up.
```
this.zoomService.initialize(SDK_KEY,SDK_SECRET,API_KEY, API_SECRET)
  .then((success: any) => console.log(success))
  .catch((error: any) => console.log(error));
```

2. Login
Log user in with Zoom username and password.
```
this.zoomService.login(userName, password)
  .then((success: any) => console.log(success))
  .catch((error: any) => console.log(error));
```
3. Logout
Log user out.
```
this.zoomService.logout()
  .then((success: boolean) => console.log(success))
  .catch((error: any) => console.log(error));
```

4. isLoggedIn
Check whether a user is logged in. Return true if the user is logged in. False if the user is not logged in.
```
this.zoomService.isLoggedIn()
  .then((success: boolean) => console.log(success))
  .catch((error: any) => console.log(error));
```

5. MeetingOptions
Meeting options. Configure the default meeting room. Some of the options are only available on Android.
```
let options = {
  custom_meeting_id: "Customized Title",
  no_share: false,
  no_audio: false,
  no_video: false,
  no_driving_mode: true,
  no_invite: true,
  no_meeting_end_message: true,
  no_dial_in_via_phone: false,
  no_dial_out_to_phone: false,
  no_disconnect_audio: true,
  no_meeting_error_message: true,
  no_unmute_confirm_dialog: true,    // Android only
  no_webinar_register_dialog: false, // Android only
  no_titlebar: false,
  no_bottom_toolbar: false,
  no_button_video: false,
  no_button_audio: false,
  no_button_share: false,
  no_button_participants: false,
  no_button_more: false,
  no_text_password: true,
  no_text_meeting_id: false,
  no_button_leave: false
 };
 ```

6. Join Meeting
Join meeting 
```
this.zoomService.joinMeeting(meetingNumber, meetingPassword, displayName, options)
  .then((success: any) => console.log(success))
  .catch((error: any) => console.log(error));
```
7. Get Users Id
Get Users Id
```
this.zoomService.getUsersId()
  .then((success: [UserId]) => console.log(success))
  .catch((error: any) => console.log(error));
```

8. Start an existing meeting for non-login user
Start an existing meeting for non-login user.
```
this.zoomService.startMeetingWithZAK(meetingNumber, displayName, userId, options)
  .then((success: any) => console.log(success))
  .catch((error: any) => console.log(error));
```

9. Start an existing meeting for logged in user
Start an existing meeting for logged in user.
```
this.zoomService.startMeeting(meetingNumber, options)
  .then((success: any) => console.log(success))
  .catch((error: any) => console.log(error));
```

10. Start an instant meeting for logged in user
Start an instant meeting for logged in user.
```
this.zoomService.startInstantMeeting()
  .then((success: any) => console.log(success))
  .catch((error: any) => console.log(error));
```

## License

Please refer to [LICENSE.md](LICENSE.md) file for details

---
Copyright ©2020 Zoom Video Communications, Inc. All rights reserved.
