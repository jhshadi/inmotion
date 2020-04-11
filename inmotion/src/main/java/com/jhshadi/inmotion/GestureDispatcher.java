package com.jhshadi.inmotion;

import org.opencv.core.Size;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

import com.jhshadi.inmotion.detectors.Area;
import com.jhshadi.inmotion.detectors.AreaRect;
import com.jhshadi.inmotion.detectors.AreasDetector;
import com.jhshadi.inmotion.detectors.BaseDetector;
import com.jhshadi.inmotion.detectors.InMotionAreasDetctorListener;
import com.jhshadi.inmotion.util.Scaler;

class GestureDispatcher implements InMotionAreasDetctorListener {

	// Consts
	private static final int DEFAULT_RADIUS_SIZE = 50;
	private static final float DEFAULT_AREA_THRESHOLD = 0.002f;
	private static final int DEFAULT_DETECTOR_THRESHOLD = 10;
	private static final int CENTER_AREA_ID = 0;
	private static final int LEFT_AREA_ID = 1;
	private static final int RIGHT_AREA_ID = 2;
	private static final int INVALID_AREA_ID = -1;

	// Obtain motion event consts
	private static final int EVENT_META_STATE = 0;
	private static final int EVENT_BUTTON_STATE = 0;
	private static final float EVENT_X_PRECISION = 1;
	private static final float EVENT_Y_PRECISION = 1;
	private static final float EVENT_PRESSURE = 1;
	private static final float EVENT_SIZE = 1;
	private static final int EVENT_TOOL_TYPE = MotionEvent.TOOL_TYPE_FINGER;
	private static final int EVENT_DEVIC_ID = 0;
	private static final int EVENT_EDGE_THRESHOLD = 10;
	private static final int EVENT_SOURCE = InputDevice.SOURCE_CLASS_POINTER;
	private static final int EVENT_FALGS = 0;

	// Data Members
	private Activity mActivity;
	private int mActualThreshold;

	private Area[] mAreas;
	private boolean mIsMotionStart;
	private int mCurrId;
	private MotionEvent mMe;
	private Point mLast;
	private Handler mHandler;

	private Area mPrimary;
	private boolean isZoom;

	private Scaler mScaler;

	// C'tor
	public GestureDispatcher(Activity activity) {
		this.mActivity = activity;
		this.mAreas = new Area[3];
		this.mIsMotionStart = false;
		this.mCurrId = -1;
		this.mMe = null;
		this.mLast = new Point(0, 0);
		this.mHandler = new Handler(Looper.getMainLooper());

		this.mPrimary = null;
		this.isZoom = false;
	}

	// Init functions
	@Override
	public BaseDetector initDetector(int frameWidth, int frameHeight) {
		AreasDetector detector = new AreasDetector();

		mAreas[CENTER_AREA_ID] = new AreaRect(0, 0, frameWidth - 1,
				frameHeight - 1);

		mAreas[LEFT_AREA_ID] = new AreaRect(0, 0, (frameWidth / 2) - 1,
				frameHeight - 1);
		mAreas[RIGHT_AREA_ID] = new AreaRect(frameWidth / 2, 0, frameWidth - 1,
				frameHeight - 1);

		for (Area area : mAreas) {
			area.setThreshold(0);
			detector.addArea(area);
		}

		detector.setDrawState(true);
		detector.setShowMovmentFrame(true);
		detector.setDiffThreshold(DEFAULT_DETECTOR_THRESHOLD);
		detector.setApplayBlur(true);

		calculateActualThreshold(frameWidth, frameHeight);
		initScaler(frameWidth, frameHeight);

		return detector;
	}

	private void calculateActualThreshold(int frameWidth, int frameHeight) {
		mActualThreshold = (int) (DEFAULT_AREA_THRESHOLD * frameWidth * frameHeight);
	}

	private void initScaler(int frameWidth, int frameHeight) {
		Display display = mActivity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mScaler = new Scaler(frameWidth, frameHeight, size.x
				+ (int) getNavigationBarSize().width, size.y);
	}

	private Size getNavigationBarSize() {
		Size result = new Size();
		Resources resources = mActivity.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_width",
				"dimen", "android");
		if (resourceId > 0) {
			result.width = resources.getDimensionPixelSize(resourceId);
		}

		resourceId = resources.getIdentifier("navigation_bar_height", "dimen",
				"android");
		if (resourceId > 0) {
			result.height = resources.getDimensionPixelSize(resourceId);
		}

		return result;
	}

	// Gesture dispatch functions
	@Override
	public void onAreaMovment(AreasDetector detector, final Area currArea) {

		// Log.i("SCALER", "scaler: " + mScaler.toString());

		if (mMe != null) {
			mMe.recycle();
			mMe = null;
		}

		setPrimary(currArea);

		new Thread(new Runnable() {

			@Override
			public void run() {
				mCurrId = getCurrAreaId(currArea);

				if (mCurrId == RIGHT_AREA_ID) {

					// Check movement
					if (isMovement(mAreas[LEFT_AREA_ID]) == true
							&& isMovement(mAreas[RIGHT_AREA_ID]) == true) {
						// Check if not contain in center point
						if (contain(mAreas[LEFT_AREA_ID].getGravityPoint(),
								mAreas[CENTER_AREA_ID].getGravityPoint(),
								DEFAULT_RADIUS_SIZE) == false
								&& contain(mAreas[RIGHT_AREA_ID]
										.getGravityPoint(),
										mAreas[CENTER_AREA_ID]
												.getGravityPoint(),
										DEFAULT_RADIUS_SIZE) == false) {

							if (isZoom == false) {
								mMe = createMultiTouchMotionEvent(
										MotionEvent.ACTION_MOVE
												| MotionEvent.ACTION_POINTER_DOWN,
										mPrimary, getSec(mPrimary));
								isZoom = true;
							}

							mMe = createMultiTouchMotionEvent(
									MotionEvent.ACTION_MOVE, mPrimary,
									getSec(mPrimary));
						}
					} else {
						if (isZoom == true) {
							if (isMovement(mAreas[LEFT_AREA_ID]) == true) {
								mPrimary = mAreas[LEFT_AREA_ID];
								mMe = createSingleTouchMotionEvent(
										MotionEvent.ACTION_POINTER_UP
												| MotionEvent.ACTION_MOVE,
										mAreas[LEFT_AREA_ID]);
							} else if (isMovement(mAreas[RIGHT_AREA_ID]) == true) {
								mPrimary = mAreas[RIGHT_AREA_ID];
								mMe = createSingleTouchMotionEvent(
										MotionEvent.ACTION_POINTER_UP
												| MotionEvent.ACTION_MOVE,
										mAreas[RIGHT_AREA_ID]);
							}
							isZoom = false;
						} else {
							// Sinagle Touch
							singleTouchHandler(mAreas[CENTER_AREA_ID]);
						}
					}

					// Sinagle Touch
					singleTouchHandler(mAreas[CENTER_AREA_ID]);
				}
			}
		}).start();
	}

	private void singleTouchHandler(Area area) {
		if (isMovement(area) == true) {

			if (mIsMotionStart == false) {
				mIsMotionStart = true;
				mMe = createSingleTouchMotionEvent(MotionEvent.ACTION_DOWN,
						area);
			} else {
				mMe = createSingleTouchMotionEvent(MotionEvent.ACTION_MOVE,
						area);
			}

			dispatchMotionEvent();

			int[] g = area.getGravityPointAsArray();
			mLast.x = g[Area.GRAVITY_POINT_X];
			mLast.y = g[Area.GRAVITY_POINT_Y];

		} else {
			if (mIsMotionStart == true) {
				mIsMotionStart = false;
				mPrimary = null;
				int[] g = area.getGravityPointAsArray();
				g[Area.GRAVITY_POINT_X] = mLast.x;
				g[Area.GRAVITY_POINT_Y] = mLast.y;
				mMe = createSingleTouchMotionEvent(MotionEvent.ACTION_UP, area);
				dispatchMotionEvent();
			}
		}
	}

	private int getCurrAreaId(Area area) {
		if (area.equals(mAreas[CENTER_AREA_ID]) == true) {
			return CENTER_AREA_ID;
		} else if (area.equals(mAreas[LEFT_AREA_ID]) == true) {
			return LEFT_AREA_ID;
		} else if (area.equals(mAreas[RIGHT_AREA_ID]) == true) {
			return RIGHT_AREA_ID;
		} else {
			return INVALID_AREA_ID;
		}
	}

	private boolean isMovement(Area area) {
		return mActualThreshold <= area.getMovementLevel();
	}

	private MotionEvent createSingleTouchMotionEvent(int meAction, Area area) {
		PointerProperties[] ppArr = { createPointerProperties(getCurrAreaId(area)) };
		PointerCoords[] pcArr = { createPointerCords(mScaler.scalePoint(area
				.getGravityPoint())) };

		return createMotionEvent(meAction, ppArr, pcArr);
	}

	private MotionEvent createMultiTouchMotionEvent(int meAction, Area area1,
			Area area2) {
		PointerProperties[] ppArr = {
				createPointerProperties(getCurrAreaId(area1)),
				createPointerProperties(getCurrAreaId(area2)) };
		PointerCoords[] pcArr = {
				createPointerCords(mScaler.scalePoint(area1.getGravityPoint())),
				createPointerCords(mScaler.scalePoint(area2.getGravityPoint())) };

		return createMotionEvent(meAction, ppArr, pcArr);
	}

	private MotionEvent createMotionEvent(int meAction,
			PointerProperties[] ppArr, PointerCoords[] pcArr) {
		return MotionEvent.obtain(android.os.SystemClock.uptimeMillis(),
				android.os.SystemClock.uptimeMillis(), meAction, ppArr.length,
				ppArr, pcArr, EVENT_META_STATE, EVENT_BUTTON_STATE,
				EVENT_X_PRECISION, EVENT_Y_PRECISION, EVENT_DEVIC_ID,
				createEdgeFlags(pcArr), EVENT_SOURCE, EVENT_FALGS);
	}

	private PointerProperties createPointerProperties(int id) {
		PointerProperties pp = new PointerProperties();
		pp.toolType = EVENT_TOOL_TYPE;
		pp.id = id;

		return pp;
	}

	private PointerCoords createPointerCords(Point p) {
		PointerCoords pc = new PointerCoords();
		pc.x = p.x;
		pc.y = p.y;
		pc.pressure = EVENT_PRESSURE;
		pc.size = EVENT_SIZE;

		return pc;
	}

	private int createEdgeFlags(PointerCoords[] pcArr) {
		int edgeFlags = 0;

		for (PointerCoords pc : pcArr) {
			if (0 <= pc.x && pc.x < EVENT_EDGE_THRESHOLD) {
				edgeFlags = edgeFlags | MotionEvent.EDGE_LEFT;
			}

			if (0 <= pc.y && pc.y < EVENT_EDGE_THRESHOLD) {
				edgeFlags = edgeFlags | MotionEvent.EDGE_TOP;
			}

			if (getFrameWidth() - EVENT_EDGE_THRESHOLD < pc.x
					&& pc.x <= getFrameWidth()) {
				edgeFlags = edgeFlags | MotionEvent.EDGE_RIGHT;
			}

			if (getFrameHeight() - EVENT_EDGE_THRESHOLD < pc.y
					&& pc.y <= getFrameHeight()) {
				edgeFlags = edgeFlags | MotionEvent.EDGE_BOTTOM;
			}
		}
		return edgeFlags;
	}

	private int getFrameWidth() {
		return mScaler.getmFromX();
	}

	private int getFrameHeight() {
		return mScaler.getmFromY();
	}

	private void dispatchMotionEvent() {
		// Dispatch event (only after all areas checked)
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mActivity.dispatchTouchEvent(mMe);
			}
		});
	}

	private boolean contain(Point p, Point pIn, int r) {
		return (pIn.x - r <= p.x && p.x <= pIn.x + r)
				&& (pIn.y - r <= p.y && p.y <= pIn.y + r);
	}

	private void setPrimary(Area currArea) {
		if (currArea.equals(mAreas[LEFT_AREA_ID]) == true
				|| currArea.equals(mAreas[RIGHT_AREA_ID]) == true) {
			if (mPrimary == null) {
				mPrimary = mAreas[getCurrAreaId(currArea)];
			}
		}
	}

	private Area getSec(Area primary) {
		if (primary.equals(mAreas[LEFT_AREA_ID]) == true) {
			return mAreas[RIGHT_AREA_ID];
		} else if (primary.equals(mAreas[RIGHT_AREA_ID]) == true) {
			return mAreas[LEFT_AREA_ID];
		}

		return null;
	}
}
