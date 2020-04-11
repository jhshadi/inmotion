package com.jhshadi.inmotion.detectors;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;


import android.graphics.Color;

public abstract class BaseDetector {

	// Consts
	protected static final boolean DEFAULT_DRAW_STATE = false;
	protected static final Scalar DEFAULT_DRAW_BORDER_COLOR = new Scalar(0,
			255, 0, 255);
	protected static final int DEFAULT_DRAW_BORDER_SIZE = 1;

	// Static
	protected static int frameWidth;
	protected static int frameHeight;
	
	// Data Members
	protected InMotionBaseDetctorListener mDetectorListener = null;


	// Getter/Setter
	public static void setFrameSize(int frameWidth, int frameHeight) {
		BaseDetector.frameWidth = frameWidth;
		BaseDetector.frameHeight = frameHeight;
	}
	
	public void setDetectorListener(InMotionBaseDetctorListener mListener) {
		this.mDetectorListener = mListener;
	}

	public InMotionBaseDetctorListener getDetectorListener() {
		return mDetectorListener;
	}
	
	// Draw Option
	protected boolean mIsDrawState = DEFAULT_DRAW_STATE;
	protected Scalar mDrawBorderColor = DEFAULT_DRAW_BORDER_COLOR;
	protected int mDrawBorderSize = DEFAULT_DRAW_BORDER_SIZE;

	public boolean getDrawState() {
		return mIsDrawState;
	}

	public void setDrawState(boolean drawState) {
		this.mIsDrawState = drawState;
	}

	public void setDrawBorderColor(int drawBorderColor) {
		this.mDrawBorderColor = new Scalar(Color.red(drawBorderColor),
				Color.green(drawBorderColor), Color.blue(drawBorderColor),
				Color.alpha(drawBorderColor));
	}

	public void setDrawBorderSize(int drawBorderSize) {
		this.mDrawBorderSize = drawBorderSize;
	}

	// Functions (Detector Interface)
	public abstract void start();

	public abstract void stop();

	public abstract void release();

	public abstract void detect(Mat matRgb, Mat matGrey);
}
