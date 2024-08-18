package dev.jhshadi.inmotion.detectors;

public interface InMotionAreasDetectorListener extends InMotionBaseDetectorListener {

    void onAreaMovement(AreasDetector detector, Area area);
}
