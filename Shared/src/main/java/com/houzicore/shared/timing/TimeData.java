package com.houzicore.shared.timing;

public class TimeData {
	public String Title;

	public long Started;
	public long LastMarker;
	public long Total;
	public int Count = 0;

	public TimeData(String title, long time) {
		Title = title;
		Started = time;
		LastMarker = time;
		Total = 0L;
	}

	public void addTime() {
		Total += System.currentTimeMillis() - LastMarker;
		LastMarker = System.currentTimeMillis();
		Count++;
	}

	public void printInfo() {
	}
}
