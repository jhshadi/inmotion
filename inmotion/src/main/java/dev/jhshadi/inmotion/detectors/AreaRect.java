package dev.jhshadi.inmotion.detectors;

public class AreaRect extends Area {

	private int left;
	private int top;
	private int right;
	private int bottom;

	public AreaRect(int left, int top, int right, int bottom) {
		super();
		
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	@Override
	public int getLeft() {
		return left;
	}

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public int getRight() {
		return right;
	}

	@Override
	public int getBottom() {
		return bottom;
	}
	
	@Override
	public int getAreaSize() {
		return (bottom - top + 1) * (right - left + 1);
	}
	
	@Override
	public boolean validateArea(int width, int height) {
		boolean isValid = false;
		
		left = validateBounds(left, MIN_WIDTH, width);
		right = validateBounds(right, MIN_WIDTH, width);
		top = validateBounds(top, MIN_HEIGHT, height);
		bottom = validateBounds(bottom, MIN_HEIGHT, height);
		
		if (validateDirection(top, bottom) && validateDirection(left, right)) {
			isValid = true;
		}
		
		return isValid;
	}
	
	private int validateBounds(int x, int xMin, int xMax) {
		if (x < xMin)
			x = xMin;
		if (xMax <= x)
			x = xMax - 1;
		
		return x;
	}
	
	private boolean validateDirection(int x1, int x2) {
		return (x1 < x2);
	}

}
