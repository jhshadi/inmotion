package dev.jhshadi.inmotion.detectors;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class AreasDetector extends BaseDetector {
	private static final String TAG = "AreasDetector::class";

	// Consts
	private static final boolean DEFAULT_IS_APPLY_BLUR = false; 			// Reduce noise but cause slow performance
	private static final boolean DEFAULT_IS_SHOW_MOVEMENT_FRAME = false;	// Show absdiff mat instead of frame (good for debug)
	private static final int MAX_DIFF_THRESHOLD = 100;						// Set the threshold for absDiff
	private static final int DEFAULT_DIFF_THRESHOLD = 10; 					// Set the threshold for absDiff

	// Data Members
	private List<Area> areas = new ArrayList<>();
	private Mat matGreyPrev;
	private Mat matGreyCur;
	private Mat matAbsDiff;
	private Mat matKernel;

	// Settings
	private boolean isApplyBlur;
	private boolean isShowMovementFrame;
	private int diffThreshold;

	// C'tor
	public AreasDetector() {
		Log.i(TAG, "Instantiated new " + this.getClass());

		isApplyBlur = DEFAULT_IS_APPLY_BLUR;
		isShowMovementFrame = DEFAULT_IS_SHOW_MOVEMENT_FRAME;
		diffThreshold = DEFAULT_DIFF_THRESHOLD;
	}

	// Getter/Setter
	public boolean isApplyBlur() {
		return isApplyBlur;
	}

	public void setApplyBlur(boolean isApplyBlur) {
		this.isApplyBlur = isApplyBlur;
	}

	public boolean isShowMovementFrame() {
		return isShowMovementFrame;
	}

	public void setShowMovementFrame(boolean isShowMovementFrame) {
		this.isShowMovementFrame = isShowMovementFrame;
	}

	public int getDiffThreshold() {
		return diffThreshold;
	}

	public void setDiffThreshold(int diffThreshold) {
		if (diffThreshold < 0) {
			this.diffThreshold = 0;
		} else if (MAX_DIFF_THRESHOLD < diffThreshold) {
			this.diffThreshold = MAX_DIFF_THRESHOLD;
		} else {
			this.diffThreshold = diffThreshold;
		}
	}

	// Area Manger Functions
	public boolean addArea(Area area) {
		boolean result = false;

		if (area.validateArea(frameWidth, frameHeight) == true) {
			areas.add(area);
			result = true;
		}

		return result;
	}

	public boolean removeArea(Area area) {
		return areas.remove(area);
	}

	// Functions
	@Override
	public void start() {
		matGreyPrev = new Mat(frameHeight, frameWidth, CvType.CV_8U);
		matGreyCur = new Mat(frameHeight, frameWidth, CvType.CV_8U);
		matAbsDiff = new Mat(frameHeight, frameWidth, CvType.CV_8U);
		matKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,
				new Size(3, 3));
	}

	@Override
	public void stop() {
		matGreyPrev.release();
		matGreyCur.release();
		matAbsDiff.release();
		matKernel.release();
	}

	@Override
	public void release() {
		// No release needed
	}

	@Override
	public void detect(Mat matRgb, Mat matGrey) {

		matGreyCur = matGrey;

		detectMotion(matGreyPrev.getNativeObjAddr(),
				matGreyCur.getNativeObjAddr(), matAbsDiff.getNativeObjAddr(),
				matKernel.getNativeObjAddr(), isApplyBlur, diffThreshold);

		// Core.absdiff(matGreyPrev, matGreyCur, matAbsDiff);
		// Imgproc.blur(matAbsDiff, matAbsDiff, new Size(7, 7));
		// Imgproc.threshold(matAbsDiff, matAbsDiff, 15, 255,
		// Imgproc.THRESH_BINARY_INV);

		matGreyPrev = matGreyCur.clone(); // need to check also how this should be released later on

		if (isShowMovementFrame == true) {
			Imgproc.cvtColor(matAbsDiff, matRgb, Imgproc.COLOR_GRAY2RGB);
		}

		for (Area area : areas) {

			if (area.isActive() == true) {

				area.setMovementLevel(checkAreaMovement(
						matAbsDiff.getNativeObjAddr(), area.getLeft(),
						area.getTop(), area.getRight(), area.getBottom(),
						area.getThreshold(), area.getGravityPointAsArray()));

				if (mIsDrawState == true) {
					Imgproc.rectangle(matRgb,
							new Point(area.getLeft(), area.getTop()),
							new Point(area.getRight(), area.getBottom()),
							mDrawBorderColor, mDrawBorderSize, 8, 0);
				}

				if (area.getMovementLevel() != Area.NO_MOVEMENT) {
					if (mIsDrawState == true) {
						Imgproc.circle(
								matRgb,
								new Point(
										area.getGravityPointAsArray()[Area.GRAVITY_POINT_X],
										area.getGravityPointAsArray()[Area.GRAVITY_POINT_Y]),
								10, new Scalar(255, 0, 0, 255));
					}

					if (mDetectorListener != null) {
						((InMotionAreasDetectorListener) mDetectorListener)
								.onAreaMovement(this, area);
					}
				}
			}
		}
	}

	// Native
	private native void detectMotion(long matAddrGr1, long matAddrGr2,
			long matAddrRes, long matKernel, boolean isApplayBlur,
			int diffThreshold);

	private native boolean calcMovementGravity(long matAddrRes, int mLeft,
			int mTop, int mRight, int mBottom, int resX, int resY);

	private native int checkAreaMovement(long matAddrRes, int mLeft, int mTop,
			int mRight, int mBottom, float mThresh, int[] mAvgPoint);
}
