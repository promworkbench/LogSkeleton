package org.processmining.logskeleton.parameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogSkeletonBrowserParameters {

	private Set<String> activities;
	private List<LogSkeletonBrowser> visualizers;
	private boolean useHyperArcs;

	public LogSkeletonBrowserParameters() {
		activities = new HashSet<String>();
		visualizers = new ArrayList<LogSkeletonBrowser>();
		setUseHyperArcs(false);
	}
	
	public Set<String> getActivities() {
		return activities;
	}

	public List<LogSkeletonBrowser> getVisualizers() {
		return visualizers;
	}

	public boolean isUseHyperArcs() {
		return useHyperArcs;
	}

	public void setUseHyperArcs(boolean useHyperArcs) {
		this.useHyperArcs = useHyperArcs;
	}
}
