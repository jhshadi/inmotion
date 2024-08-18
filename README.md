# InMotion
InMotion is an Android library that transforms your Android device into a motion sensing controller and lets you control your android device using gestures and movement.
The library uses the device camera or an external camera to detect motion and to track faces and objects in the frame and notifies your app once movement has occurred.
By using InMotion library, Android developers can now create motion based apps easily and quickly.

InMotion uses OpenCV library, extends it and completely encapsulates it to introduce a simple and easy-to-use API specific for motion detection purposes with an emphasis on full functionality and high performance.

[![InMotion & Face Detection Library For The Android](https://img.youtube.com/vi/K4rInX2TF5I/0.jpg)](https://www.youtube.com/watch?v=K4rInX2TF5I)

## Library Permissions
The library uses the following camera permissions:
- `android.hardware.camera`
- `android.hardware.camera.autofocus`
- `android.hardware.camera.front`
- `android.hardware.camera.front.autofocus`

When using it with your app there's no need to declare those permissions, the `inmotion` instance will request for those for you.
Note that upon user decline for the above permissions there's the app owner will need to handle the app degradation.  


### TODO:
- Need to fix camera size functionality (size always shown as 1024x720)
- Migrate to Android CameraX instead of using opencv implementation for the surfaceview and camera2 API's
