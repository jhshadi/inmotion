package com.jhshadi.inmotion.detectors;

public class AreaCircle extends Area {

	private int x;
	private int y;
	private int r;

	public AreaCircle(int x, int y, int radius, int treshold) {
		super();

		this.x = x;
		this.y = y;
		this.r = radius;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getRadius() {
		return r;
	}

	@Override
	public int getLeft() {
		return x - r;
	}

	@Override
	public int getTop() {
		return y - r;
	}

	@Override
	public int getRight() {
		return x + r;
	}

	@Override
	public int getBottom() {
		return y + r;
	}

	@Override
	public int getAreaSize() {
		return (int) (Math.PI * (r * r));
	}
	
	@Override
	public boolean validateArea(int width, int height) {
		return (MIN_WIDTH <= x-r  && x+r < width) && (MIN_HEIGHT <= y-r  && y+r < height);
	}
}
