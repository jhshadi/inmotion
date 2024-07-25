package dev.jhshadi.inmotion.util;

import android.graphics.Point;

public class Scaler {

	private int mFromX;
	private int mFromY;

	private int mToX;
	private int mToY;

	private double mScaleX;
	private double mScaleY;

	public Scaler(int fromWidth, int fromHeight, int toWidth, int toHeight) {
		this.mFromX = fromWidth;
		this.mFromY = fromHeight;

		this.mToX = toWidth;
		this.mToY = toHeight;

		mScaleX = getScale(mFromX, mToX);
		mScaleY = getScale(mFromY, mToY);
	}

	public int getmFromX() {
		return mFromX;
	}

	public void setmFromX(int mFromX) {
		this.mFromX = mFromX;
	}

	public int getmFromY() {
		return mFromY;
	}

	public void setmFromY(int mFromY) {
		this.mFromY = mFromY;
	}

	public int getmToX() {
		return mToX;
	}

	public void setmToX(int mToX) {
		this.mToX = mToX;
	}

	public int getmToY() {
		return mToY;
	}

	public void setmToY(int mToY) {
		this.mToY = mToY;
	}

	public double getmScaleX() {
		return mScaleX;
	}

	public void setmScaleX(double mScaleX) {
		this.mScaleX = mScaleX;
	}

	public double getmScaleY() {
		return mScaleY;
	}

	public void setmScaleY(double mScaleY) {
		this.mScaleY = mScaleY;
	}

	public static double getScale(int from, int to) {
		return (double)to / (double)from ;
	}

	public double scaleX(int scaleX) {
		return scaleX * mScaleX;
	}

	public double scaleY(int scaleY) {
		return scaleY * mScaleY;
	}

	public Point scalePoint(Point p) {
		return new Point((int) scaleX(p.x), (int) scaleY(p.y));
	}

	@Override
	public String toString() {
		return "Scaler\nFromX: " + mFromX + "\tToX: " + mToX + "\tScaleX: "
				+ mScaleX + "\nFromY: " + mFromY + "\tToY: " + mToY + "\tScaleY: "
				+ mScaleY;
	}
}
