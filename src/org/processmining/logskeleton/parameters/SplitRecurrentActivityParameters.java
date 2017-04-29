package org.processmining.logskeleton.parameters;

import java.util.HashSet;
import java.util.Set;

public class SplitRecurrentActivityParameters {

	private Set<String> milestoneActivities;
	private String recurrentActivity;
	
	public SplitRecurrentActivityParameters() {
		milestoneActivities = new HashSet<String>();
	}
	public Set<String> getMilestoneActivities() {
		return milestoneActivities;
	}
	
	public String getDuplicateActivity() {
		return recurrentActivity;
	}
	
	public void setDuplicateActivity(String duplicateActivity) {
		this.recurrentActivity = duplicateActivity;
	}
}
