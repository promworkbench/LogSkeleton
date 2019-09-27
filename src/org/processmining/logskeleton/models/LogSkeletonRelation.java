package org.processmining.logskeleton.models;

public enum LogSkeletonRelation {

	RESPONSE("Response"),
	PRECEDENCE("Precedence"),
	NOTRESPONSE("Not Response"),
	NOTPRECEDENCE("Not Precedence"),
	NOTCOEXISTENCE("Not Co-Existence");
	
	private String label;

	private LogSkeletonRelation(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
}
