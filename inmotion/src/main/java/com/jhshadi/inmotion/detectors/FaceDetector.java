package com.jhshadi.inmotion.detectors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgproc.Imgproc;

import com.jhshadi.inmotion.R;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

public class FaceDetector extends BaseDetector {
	private static final String TAG = "FaceDetector::class";

	// Consts
	private static final float DEFAULT_RELATIVE_FACE_SIZE = 0.1f;
	private static final boolean DEFAULT_IS_FACE_LOCK_ACTIVE = false;
	private static final double DEFAULT_FACE_LOCK_SCALAR = 2;

	// Data Members
	private long mNativeObj = 0;
	private float mRelativeFaceSize = DEFAULT_RELATIVE_FACE_SIZE;
	private int mAbsoluteFaceSize = 0;
	private ArrayList<Face> mFacesArray;

	// Face Lock
	private boolean mIsFaceLockActive = DEFAULT_IS_FACE_LOCK_ACTIVE;
	private ArrayList<Face> mLockedFaces = new ArrayList<>();

	// C'tor
	public FaceDetector(Context appContext) {
		this(appContext, 0);
	}

	public FaceDetector(Context appContext, int minFaceSize) {
		Log.i(TAG, "Instantiated new " + this.getClass());

		try {
			// load cascade file
			InputStream is = appContext.getResources().openRawResource(
					R.raw.lbpcascade_frontalface_improved);

			Log.i(TAG, "InputStream is " + is);
			File cascadeDir = appContext
					.getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile = new File(cascadeDir,
					"lbpcascade_frontalface_improved.xml");
			FileOutputStream os = new FileOutputStream(cascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}

			//TODO: should be in the try block or at least in a finally block
			is.close();
			os.close();

			mNativeObj = nativeCreateObject(cascadeFile.getAbsolutePath(),
					minFaceSize);

			cascadeDir.delete();

		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
		}
	}

	// Getter/Setter
	public void setRelativeFaceSize(float mRelativeFaceSize) {
		this.mRelativeFaceSize = mRelativeFaceSize;
		this.mAbsoluteFaceSize = 0;
	}

	public float getRelativeFaceSize() {
		return mRelativeFaceSize;
	}

	public int getAbsoluteFaceSize() {
		return mAbsoluteFaceSize;
	}

	// Lock Face Functions
	public void setFaceLockActiveState(boolean isFaceLockActive) {
		this.mIsFaceLockActive = isFaceLockActive;
	}

	public boolean getFaceLockActiveState() {
		return mIsFaceLockActive;
	}

	public boolean lockFace(Face face) {

		for (Face f : mLockedFaces) {
			if (f.contains(face.getCenter(), DEFAULT_FACE_LOCK_SCALAR) == true) {
				return false;
			}
		}

		mLockedFaces.add(face);
		return true;
	}

	public boolean unlockFace(Face face) {
		return mLockedFaces.remove(face);
	}

	public void clearLockedFaces() {
		mLockedFaces.clear();
	}

	// Functions
	@Override
	public void start() {
		nativeStart(mNativeObj);
		// matResult = new Mat(frameHeight, frameWidth, CvType.CV_32FC1);
	}

	@Override
	public void stop() {
		nativeStop(mNativeObj);
		// matResult.release();
	}

	@Override
	public void release() {
		nativeDestroyObject(mNativeObj);
		mNativeObj = 0;
	}

	@Override
	public void detect(Mat matRgb, Mat matGrey) {
		if (mAbsoluteFaceSize == 0) {
			setMinFaceSize(matGrey.rows());
		}

		MatOfRect faces = new MatOfRect();

		nativeDetect(mNativeObj, matGrey.getNativeObjAddr(),
				faces.getNativeObjAddr());

		mFacesArray = RectUtils.toFaceArray(faces.toArray());
		// for (Face face : mFacesArray) {
		// face.setMat(matGrey);
		// }

		if (mIsFaceLockActive == true) {
			filterFaces(matGrey);
			mFacesArray = mLockedFaces;
		}

		for (Face face : mFacesArray) {
			// Mat cropedFace = matRgb.submat(face);

			if (mIsDrawState == true) {
				if (mIsFaceLockActive == false
						|| (mIsFaceLockActive == true && face.isLockActive() == true)) {
					Imgproc.rectangle(matRgb, face.getRect().tl(), face.getRect()
							.br(), mDrawBorderColor, mDrawBorderSize);
				}
			}

			if (mDetectorListener != null) {
				((InMotionFaceDetectorListener) mDetectorListener)
						.onFaceMovement(this, face);
			}
		}

	}

	private void filterFaces(Mat matGrey) {
		for (int i = 0; i < mLockedFaces.size(); i++) {
			findNearByFacesContainAlgo(i, matGrey);
		}
	}

	// Contain algo
	private void findNearByFacesContainAlgo(int faceToFindIndex, Mat matGrey) {
		Face faceToFind = mLockedFaces.get(faceToFindIndex);
		faceToFind.setLockActive(false);

		for (int i = 0; i < mFacesArray.size(); i++) {
			Face face = mFacesArray.get(i);

			if (face.getRect() != null) {
				if (faceToFind.contains(face.getCenter(),
						DEFAULT_FACE_LOCK_SCALAR) == true) {
					mFacesArray.remove(i);
					faceToFind.set(face);
					faceToFind.setLockActive(true);
					return;
				}
			}
		}
	}

	// Closest Center algo
	private void findNearByFacesDistanceAlgo(int faceToFindIndex, Mat matGrey) {
		Face faceToFind = mLockedFaces.get(faceToFindIndex);
		int closestFaceIndex = -1;
		double distance;
		double minDistance = Double.MAX_VALUE;
		double distanceLimit = faceToFind.getDistance(new Point(faceToFind
				.getRight(), faceToFind.getBottom()))
				* 1.5;

		faceToFind.setLockActive(false);

		for (int i = 0; i < mFacesArray.size(); i++) {
			Face face = mFacesArray.get(i);

			if (face.getRect() != null) {
				distance = faceToFind.getDistance(face.getCenter());

				if (distance < minDistance && distance < distanceLimit) {
					minDistance = distance;
					closestFaceIndex = i;
				}
			}
		}

		if (closestFaceIndex != -1) {
			Face closestFace = mFacesArray.remove(closestFaceIndex);
			faceToFind.set(closestFace);
			faceToFind.setLockActive(true);
		}
	}

	// Diff algo (Intense Processing)
	private Mat matResult;

	private void findNearByFacesTMAlgo(int faceToFindIndex, Mat matGrey) {
		Face faceToFind = mLockedFaces.get(faceToFindIndex);
		faceToFind.setLockActive(false);

		Imgproc.matchTemplate(matGrey, faceToFind.getMat(), matResult,
				Imgproc.TM_CCORR);
		MinMaxLocResult mmr = Core.minMaxLoc(matResult);

		for (int i = 0; i < mFacesArray.size(); i++) {
			Face face = mFacesArray.get(i);

			if (face.getRect() != null) {
				if (face.contains(new Point((int) mmr.minLoc.x,
						(int) mmr.minLoc.y), 1) == true) {
					Face newFace = mFacesArray.remove(i);
					faceToFind.set(newFace);
					faceToFind.setLockActive(true);
					return;
				}
			}
		}
	}

	public void setMinFaceSize(int frameHeight) {
		if (mAbsoluteFaceSize == 0) {
			if (Math.round(frameHeight * mRelativeFaceSize) > 0) {
				mAbsoluteFaceSize = Math.round(frameHeight * mRelativeFaceSize);
			}
			setMinFaceSize(mAbsoluteFaceSize);
		}

		nativeSetFaceSize(mNativeObj, mAbsoluteFaceSize);
	}

	// Native
	private static native long nativeCreateObject(String cascadeName,
			int minFaceSize);

	private static native void nativeDestroyObject(long thiz);

	private static native void nativeStart(long thiz);

	private static native void nativeStop(long thiz);

	private static native void nativeSetFaceSize(long thiz, int size);

	private static native void nativeDetect(long thiz, long inputImage,
			long faces);
}
