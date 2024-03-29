package com.jhshadi.inmotion;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import com.jhshadi.inmotion.detectors.BaseDetector;
import com.jhshadi.inmotion.detectors.InMotionBaseDetctorListener;

public class InMotion {
    private static final String TAG = "InMotion::class";

    // Consts
    private static final int DEFAULT_MAX_FRAME_WIDTH = 720;
    private static final int DEFAULT_MAX_FRAME_HEIGHT = 720;
    private static final boolean DEFAULT_IS_INMOTION_EVENTS_ON = true;
    private static final boolean DEFAULT_IS_FLIP_FRAME = false;

    // Data Members
    private InMotionCameraViewListener mCameraViewListener;

    private Activity mAppContext;
    private InMotionBaseDetctorListener mInMotionInitDetectorListener;
    private InMotionJavaCameraView mOpenCvCameraView;
//    private InMotionBaseLoaderCallback mLoaderCallback;

    private BaseDetector mDetector;
    private Mat mRgba;
    private Mat mGray;

    private GestureDispatcher mGestDisp;

    private boolean isInMotionEventsOn;
    private boolean isFlipFrame;

    public InMotion(Activity appContext,
                    InMotionBaseDetctorListener inMotionInitDetectorListener,
                    InMotionJavaCameraView inMotionJavaCameraView) {
        this(appContext, inMotionInitDetectorListener, inMotionJavaCameraView,
                DEFAULT_MAX_FRAME_WIDTH, DEFAULT_MAX_FRAME_HEIGHT);
    }

    public InMotion(Activity appContext,
                    InMotionBaseDetctorListener inMotionInitDetectorListener,
                    InMotionJavaCameraView inMotionJavaCameraView, int maxFrameWidth,
                    int maxFrameHeight) {
        Log.i(TAG, "Instantiated new " + this.getClass());

        this.isInMotionEventsOn = DEFAULT_IS_INMOTION_EVENTS_ON;
        this.isFlipFrame = DEFAULT_IS_FLIP_FRAME;
        this.mAppContext = appContext;

        if (inMotionInitDetectorListener == null) {
            mGestDisp = new GestureDispatcher(appContext);
            inMotionInitDetectorListener = mGestDisp;
        }
        this.mInMotionInitDetectorListener = inMotionInitDetectorListener;

        this.mCameraViewListener = new InMotionCameraViewListener();

        this.mOpenCvCameraView = inMotionJavaCameraView;
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(maxFrameWidth, maxFrameHeight);
        mOpenCvCameraView.setCvCameraViewListener(mCameraViewListener);

//        mLoaderCallback = new InMotionBaseLoaderCallback(mAppContext);
    }

    public void start() {
//        System.loadLibrary("opencv_java4");

        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV loaded successfully");

            // Load native library after(!) OpenCV initialization
            System.loadLibrary("inMotion");

            mOpenCvCameraView.enableView();
        } else {
            Log.e(TAG, "Unable to load OpenCV!");
        }
    }

    public void stop() {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void release() {
        stop();

        if (mDetector != null) {
            mDetector.release();
            mDetector = null;
        }
    }

    // InMotion Loader (openCV manager & native lib)
//    private class InMotionBaseLoaderCallback extends BaseLoaderCallback {
//        private static final String TAG = "InMotion::class";
//
//        public InMotionBaseLoaderCallback(Context AppContext) {
//            super(AppContext);
//        }
//
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS: {
//                    Log.i(TAG, "OpenCV loaded successfully");
//
//                    // Load native library after(!) OpenCV initialization
//                    System.loadLibrary("inMotion");
//
//                    mOpenCvCameraView.enableView();
//
//                }
//                break;
//                default: {
//                    super.onManagerConnected(status);
//                }
//                break;
//            }
//        }
//    }

    // InMotion Camera View Listener
    private class InMotionCameraViewListener implements CvCameraViewListener2 {

        public void onCameraViewStarted(int width, int height) {
            mGray = new Mat(height, width, CvType.CV_8U);
            mRgba = new Mat(height, width, CvType.CV_8UC4);

            BaseDetector.setFrameSize(width, height);

            // Run only once if detector is uninstantiated
            if (mDetector == null) {
                if (mInMotionInitDetectorListener != null
                        && (mDetector = mInMotionInitDetectorListener
                        .initDetector(width, height)) != null) {
                    mDetector
                            .setDetectorListener(mInMotionInitDetectorListener);
                    mInMotionInitDetectorListener = null;
                    mDetector.start();
                }
            } else {
                // Must run every time the camera start
                mDetector.start();
            }
        }

        public void onCameraViewStopped() {
            mGray.release();
            mRgba.release();

            if (mDetector != null) {
                mDetector.stop();
            }
        }

        public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();

            if (mDetector != null && isInMotionEventsOn == true) {
                mDetector.detect(mRgba, mGray);
            }

            if (isFlipFrame == true) {
                Core.flip(mRgba, mRgba, 1);
            }

            return mRgba;
        }

    }

    // Functions
    public BaseDetector getDetector() {
        return mDetector;
    }

    public void startInMotionEvents() {
        this.isInMotionEventsOn = true;
    }

    public void stopInMotionEvents() {
        this.isInMotionEventsOn = false;
    }

    public boolean getFlipFrame() {
        return isFlipFrame;
    }

    public void setFlipFrame(boolean isFlipFrame) {
        this.isFlipFrame = isFlipFrame;
    }

}