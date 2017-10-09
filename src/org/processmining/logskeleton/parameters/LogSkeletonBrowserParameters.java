package org.processmining.logskeleton.parameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogSkeletonBrowserParameters {

	private Set<String> activities;
	private List<LogSkeletonBrowser> visualizers;

	public LogSkeletonBrowserParameters() {
		activities = new HashSet<String>();
		visualizers = new ArrayList<LogSkeletonBrowser>();
	}
	public Set<String> getActivities() {
		return activities;
	}

	public List<LogSkeletonBrowser> getVisualizers() {
		return visualizers;
	}
}
