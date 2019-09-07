package org.androidtown.quicknavi.stage05;

import java.io.Serializable;

public class GPoint implements Serializable {

	private static final long serialVersionUID = -1171376646814264988L;

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
