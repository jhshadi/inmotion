package com.jhshadi.inmotion.detectors;

import android.graphics.Point;

public abstract class Area {

	public static final int NO_MOVEMENT = -1;
	public static final int GRAVITY_POINT_X = 0;
	public static final int GRAVITY_POINT_Y = 1;
	private static final int MAX_THRESHOLD = 100;
	private static final int DEFAULT_MOTION_THRESHOLD = 20;
	private static final boolean DEFAULT_IS_ACTIVE = true;
	protected static final int MIN_WIDTH = 0;
	protected static final int MIN_HEIGHT = 0;

	private boolean active;
	private float threshold;
	private int movementLevel;
	private int[] gravityPoint;

	public Area() {
		this.threshold = DEFAULT_MOTION_THRESHOLD;
		this.active = DEFAULT_IS_ACTIVE;
		this.movementLevel = NO_MOVEMENT;
		this.gravityPoint = new int[2];
	}

	// Package Functions
	void setMovementLevel(int movementLevel) {
		this.movementLevel = movementLevel;
	}

	// Public Functions
	public int[] getGravityPointAsArray() {
		return gravityPoint;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setThreshold(float threshold) {
		if (threshold < 0) {
			this.threshold = 0;
		} else if (MAX_THRESHOLD < threshold) {
			this.threshold = MAX_THRESHOLD;
		} else {
			this.threshold = threshold;
		}
	}

	public boolean isActive() {
		return active;
	}

	public float getThreshold() {
		return threshold;
	}

	public int getMovementLevel() {
		return movementLevel;
	}

	public Point getGravityPoint() {
		return new Point(gravityPoint[GRAVITY_POINT_X],
				gravityPoint[GRAVITY_POINT_Y]);
	}

	public abstract int getLeft();

	public abstract int getTop();

	public abstract int getRight();

	public abstract int getBottom();

	public abstract int getAreaSize();

	public abstract boolean validateArea(int width, int height);
}
