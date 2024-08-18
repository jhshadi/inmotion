package dev.jhshadi.inmotion.detectors;

public interface InMotionFaceDetectorListener extends InMotionBaseDetectorListener {

    void onFaceMovement(FaceDetector detector, Face face);
}
