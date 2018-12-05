package org.processmining.logskeleton.parameters;

public enum LogSkeletonBrowser {
	
//	ALWAYSTOGETHER("Always Together"),
	ALWAYSAFTER("Response"),
	ALWAYSBEFORE("Precedence"),
	OFTENNEXT("Often Next"),
	OFTENPREVIOUS("Often Previous"),
//	NEVERTOGETHERSELF("Never Together (Self)"),
	NEVERTOGETHER("Not Co-existence"),
//	NEVERBEFORE("Never Before"),
//	NEVERAFTER("Never After"),
//	SOMETIMESBEFORE("Sometimes Before"),
//	SOMETIMESAFTER("Sometimes After"),
	NEXTONEWAY("Next (One Way)"),
	NEXTBOTHWAYS("Next (Both Ways)");
	
	private String label;

	private LogSkeletonBrowser(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
}
