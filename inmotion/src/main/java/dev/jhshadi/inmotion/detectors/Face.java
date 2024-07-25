package dev.jhshadi.inmotion.detectors;

import android.graphics.Point;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class Face {

	private Mat mat;
	private Rect r;
	private Point pc;
	private boolean isLockActive;

	public Face() {
		this.isLockActive = false;
		this.pc = new Point(-1, -1);
	}

	public Face(Rect r) {
		this();
		setRect(r);
	}
	
	void set(Face face) {
		
		this.mat = face.mat;
		this.r = face.r;
		this.pc = face.pc;
		this.isLockActive = face.isLockActive;
	}

	void setRect(Rect r) {
		this.r = r;
		if (r != null) {
			this.pc.set(r.x + (r.width / 2), r.y + (r.height / 2));
		} else {
			this.pc.set(-1, -1);
		}
	}

	Rect getRect() {
		return r;
	}

	void setMat(Mat mat) {
		this. mat = mat.submat(r);
	}

	Mat getMat() {
		return mat;
	}

	void setLockActive(boolean isLockActive) {
		this.isLockActive = isLockActive;
	}

	boolean isLockActive() {
		return isLockActive;
	}

	// Public Functions
	public int getTop() {
		return r.y;
	}

	public int getLeft() {
		return r.x;
	}

	public int getBottom() {
		return r.y + r.height;
	}

	public int getRight() {
		return r.x + r.width;
	}

	public int getCenterX() {
		return pc.x;
	}

	public int getCenterY() {
		return pc.y;
	}

	public Point getCenter() {
		return pc;
	}

	public double getDistance(Point p) {
		return Math.sqrt((pc.x - p.x) * (pc.x - p.x) + (pc.y - p.y)
				* (pc.y - p.y));
	}

	public boolean contains(Point p, double scalar) {

		if (scalar != 1) {

			int scalarPixelsWidth = (int) ((r.width - (r.width * scalar)) / 2);
			int scalarPixelsHeight = (int) ((r.height - (r.height * scalar)) / 2);

			return contains(r.x + scalarPixelsWidth, r.y + scalarPixelsHeight,
					(int) (r.width * scalar), (int) (r.height * scalar), p);

		} else {
			return contains(r.x, r.y, r.width, r.height, p);
		}
	}

	private boolean contains(int x, int y, int width, int height, Point p) {
		return x <= p.x && p.x < x + width && y <= p.y && p.y < y + height;
	}
}