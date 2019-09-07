package org.androidtown.quicknavi.stage05;

import java.io.Serializable;

public class LocationInfo implements Serializable {

	private static final long serialVersionUID = 3699442826697376851L;

	private String name;

	private String tel;

	private String address;

	private String x;

	private String y;

	public LocationInfo() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

}
