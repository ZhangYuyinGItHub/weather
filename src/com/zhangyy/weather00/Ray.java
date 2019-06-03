package com.zhangyy.weather00;

public class Ray {
	private String mRayValue;
	private String mRayTime;
	private String mRayId;

	public Ray(String value, String time) {
		mRayValue = value;
		mRayTime = time;
	}
	
	public void setRayId(String id)
	{
		mRayId = id;
	}
	
	public String getId() {
		return mRayId;
	}

	public String getValue() {
		return mRayValue;
	}

	public String getTime() {
		return mRayTime;
	}

}
