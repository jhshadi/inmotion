# InMotion

### TODO:
- need to add those variables to `local.properties`:
```
ndk.dir=<path to ndk (current version 21.0.6113669)>
sdk.dir=<path to sdk>

opencvsdk.dir=<path to opencv root dir>
```
- Need to change the way opencv is imported to the project (from the Android Studio way to the opencv way.. full instructions are in the `build.gradle` under the opencv project)
- Migrate to Android CameraX instead of using opencv implementation for the surfaceview and camera2 API's
