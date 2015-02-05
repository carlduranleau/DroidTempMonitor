package com.neurodesign.android.application.database;

public class DataBean {
	private double mTemperature = 0;
	private double mMaxTemperature = 25;
	private double mHumidity = 0;
	private String mUnit = "0"; //"0" = Celsius (Default)
	
	public double getTemperature() {
		return mTemperature;
	}
	public void setTemperature(Double mTemperature) {
		this.mTemperature = mTemperature;
	}
	public double getMaxTemperature() {
		return mMaxTemperature;
	}
	public void setMaxTemperature(double mMaxTemperature) {
		this.mMaxTemperature = mMaxTemperature;
	}
	public double getHumidity() {
		return mHumidity;
	}
	public void setHumidity(double mHumidity) {
		this.mHumidity = mHumidity;
	}
	public String getUnit() {
		return mUnit;
	}
	public void setUnit(String mUnit) {
		this.mUnit = mUnit;
	}
}
