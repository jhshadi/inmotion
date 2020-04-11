package com.jhshadi.inmotion;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;

import org.opencv.android.JavaCamera2View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InMotionJavaCameraView extends JavaCamera2View {
    private static final String TAG = "InMotionJavaCameraView::class";

    public InMotionJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    // Extract Android Camera Object
//	public Camera getCamera() {
//		return mCameraDevice;
//	}

    // Set\Get Resolution
    public List<Size> getResolutionList() {
        try {
            CameraManager camManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            String cameraId = camManager.getCameraIdList()[mCameraIndex];
            StreamConfigurationMap map = camManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = map.getOutputSizes(ImageReader.class);

            return Arrays.asList(sizes);
        } catch (CameraAccessException e) {
            return Collections.emptyList();
        }
    }

    public void setResolution(Size resolution) {
        disconnectCamera();
        setMaxFrameSize(resolution.getWidth(), resolution.getHeight());
        connectCamera(getWidth(), getHeight());
    }

	public Size getResolution() {
		return new Size(mFrameWidth, mFrameHeight);
	}

    // Control Flash
	public boolean turnFlashOn() {
        return toggleFlash(true);
	}

    public boolean turnFlashOff() {
        return toggleFlash(false);
    }

    // Strech preview frame
    public void strechPreviewSizeWidth(int width) {
        setScale(mFrameWidth, width);
    }

    public void strechPreviewSizeHeight(int height) {
        setScale(mFrameHeight, height);
    }

    private boolean toggleFlash(boolean isFlashEnabled) {
        boolean result = false;

        try {
            CameraManager camManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
            String cameraId = camManager.getCameraIdList()[mCameraIndex];
            if (camManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                camManager.setTorchMode(cameraId, isFlashEnabled);
                result = true;
            }
        } catch (CameraAccessException e) {
            // nothing to be done
        }

        return result;
    }

    private void setScale(int oldSize, int newSize) {
        mScale = newSize / (float) oldSize;
    }
}
