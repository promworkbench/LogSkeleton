package org.processmining.logskeleton.models;

public enum LogSkeletonRelation {

	ALWAYSAFTER("Response"),
	ALWAYSBEFORE("Precedence"),
	NEVERAFTER("Not Response"),
	NEVERBEFORE("Not Precedence"),
	NEVERTOGETHER("Not Co-Existence");
	
	private String label;

	private LogSkeletonRelation(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
}
