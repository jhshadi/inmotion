package com.jhshadi.inmotion.detectors;


public interface InMotionFaceDetectorListener extends InMotionBaseDetctorListener {

	public void onFaceMovement(FaceDetector detector, Face face);
}
