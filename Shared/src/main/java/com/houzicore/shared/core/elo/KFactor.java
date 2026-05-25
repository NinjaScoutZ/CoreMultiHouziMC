package com.houzicore.shared.core.elo;

public class KFactor {
	public int startIndex, endIndex;
	public double value;

	public KFactor(int startIndex, int endIndex, double value) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.value = value;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "kfactor: " + startIndex + " " + endIndex + " " + value;
	}
}
