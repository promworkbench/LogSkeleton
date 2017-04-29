package org.processmining.logskeleton.parameters;

import java.util.HashSet;
import java.util.Set;

public class LogSkeletonBrowserParameters {

	private Set<String> activities;
	private Set<LogSkeletonBrowser> visualizers;

	public LogSkeletonBrowserParameters() {
		activities = new HashSet<String>();
		visualizers = new HashSet<LogSkeletonBrowser>();
	}
	public Set<String> getActivities() {
		return activities;
	}

	public Set<LogSkeletonBrowser> getVisualizers() {
		return visualizers;
	}
}
