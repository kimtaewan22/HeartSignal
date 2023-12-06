
# About HeartSignal

### *Overview*

> This is a CPR Training program

### *Development background of project*

> The primary objective of HeartSignal is to enhance the emergency response capabilities to increase the survival rate of current cardiac arrest patients. According to statistics from the Korea Centers for Disease Control and Prevention, there are 64.7 cases of cardiac arrest patients per 100,000 population, and this number is steadily increasing.

> When cardiopulmonary resuscitation (CPR) is performed by bystanders, the survival rate is 11.6%, more than twice as high as when CPR is not administered. However, as of 2021, the rate of CPR performed by the general public in South Korea stands at only 28.8%, which is significantly lower compared to other advanced countries such as the United States, the United Kingdom, and Japan.

> We have identified a major reason for this low CPR rate, which is the lack of proper knowledge and awareness about emergency procedures. Therefore, HeartSignal focuses on providing appropriate education and guidance to enable individuals to perform correct CPR in emergency situations, with the goal of improving the survival rate of cardiac arrest patients. Through these efforts, we aim to enhance the emergency response capabilities in South Korea, reduce the survival rate gap with advanced countries, and ultimately save more lives.


## Build the demo using Android Studio

### *Prerequisites*

*   The **[Android Studio](https://developer.android.com/studio/index.html)** IDE. This sample has been tested on Android Studio Dolphin.

*   A physical Android device with a minimum OS version of SDK 24 (Android 7.0 -
    Nougat) with developer mode enabled. The process of enabling developer mode
    may vary by device.

### *Building*

*   Open Android Studio. From the Welcome screen, select Open an existing
    Android Studio project.

*   With your Android device connected to your computer and developer mode
    enabled, click on the green Run arrow in Android Studio.

## *Dependency*

#### CameraX Library

    // CameraX core library
    def camerax_version = '1.2.0-alpha02'
    implementation "androidx.camera:camera-core:$camerax_version"
    // CameraX Camera2 extensions
    implementation "androidx.camera:camera-camera2:$camerax_version"
    // CameraX Lifecycle library
    implementation "androidx.camera:camera-lifecycle:$camerax_version"
    // CameraX View class
    implementation "androidx.camera:camera-view:$camerax_version"

#### Chart Library

    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

#### Couroutine

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

        

### Models used

*you need three models*

> 1. pose_landmaker_full
> 2. pose_landmaker_heavy
> 3. pose_landmaker_lite

---
### Stack
> 1. Kotlin
> 2. C++ (with Arduino)
> 3. firebase

### Scenario

*We have created a CPR emergency training scenario composed of three stages*

1. Speech Recognition
2. Chest Compressions(with The correct angle of the arm) 

### DEMO

https://github.com/kimtaewan22/HeartSignal/assets/85469656/1f66a831-b044-4b79-8d08-77625fa0c670




