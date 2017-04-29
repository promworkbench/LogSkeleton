package org.processmining.logskeleton.parameters;

import java.util.HashSet;
import java.util.Set;

public class ViewLogSkeletonParameters {

	private Set<String> activities;
	private Set<ViewLogSkeleton> visualizers;

	public ViewLogSkeletonParameters() {
		activities = new HashSet<String>();
		visualizers = new HashSet<ViewLogSkeleton>();
	}
	public Set<String> getActivities() {
		return activities;
	}

	public Set<ViewLogSkeleton> getVisualizers() {
		return visualizers;
	}
}
