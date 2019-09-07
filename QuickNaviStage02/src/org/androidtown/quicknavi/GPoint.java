package org.androidtown.quicknavi;

public class GPoint {

	public double x;
	
	public double y;
	
	public double z;
	
	/**
	 * Constructor
	 */
	public GPoint() {
		super();
	}

	/**
	 * Constructor
	 */
	public GPoint(double x, double y) {
		this.x = x;
		this.y = y;
		this.z = 0.0D;
	}

	/**
	 * Constructor
	 */
	public GPoint(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	
	
}
