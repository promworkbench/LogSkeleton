package org.processmining.logskeleton.parameters;

public enum LogSkeletonBrowser {
	
//	ALWAYSTOGETHER("Always Together"),
	ALWAYSAFTER("Always After"),
	ALWAYSBEFORE("Always Before"),
	OFTENNEXT("Often Next"),
	OFTENPREVIOUS("Often Previous"),
//	NEVERTOGETHERSELF("Never Together (Self)"),
	NEVERTOGETHER("Never Together"),
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
