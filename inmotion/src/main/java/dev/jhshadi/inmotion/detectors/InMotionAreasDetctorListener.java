package dev.jhshadi.inmotion.detectors;


public interface InMotionAreasDetctorListener extends InMotionBaseDetctorListener {

    public void onAreaMovment(AreasDetector detector, Area area);
}
