package org.processmining.logskeleton.parameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogSkeletonBrowserParameters {

	private Set<String> activities;
	private List<LogSkeletonBrowser> visualizers;
	private boolean useHyperArcs;
	private boolean useFalseConstraints;
	private boolean useEdgeColors;
	private boolean useEquivalenceClass;
	private boolean useNeighbors;

	public LogSkeletonBrowserParameters() {
		activities = new HashSet<String>();
		visualizers = new ArrayList<LogSkeletonBrowser>();
		/* 
		 * By default, do not use hyper arcs as finding the hyper arcs may take considerable time. 
		 */
		setUseHyperArcs(false);
		setUseFalseConstraints(true);
		setUseEdgeColors(true);
		setUseEquivalenceClass(true);
		setUseNeighbors(true);
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

	public boolean isUseFalseConstraints() {
		return useFalseConstraints;
	}

	public void setUseFalseConstraints(boolean useFalseConstraints) {
		this.useFalseConstraints = useFalseConstraints;
	}

	public boolean isUseNeighbors() {
		return useNeighbors;
	}

	public void setUseNeighbors(boolean useNeighbors) {
		this.useNeighbors = useNeighbors;
	}

	public boolean isUseEdgeColors() {
		return useEdgeColors;
	}

	public void setUseEdgeColors(boolean useEdgeColors) {
		this.useEdgeColors = useEdgeColors;
	}

	public boolean isUseEquivalenceClass() {
		return useEquivalenceClass;
	}

	public void setUseEquivalenceClass(boolean useEquivalenceClass) {
		this.useEquivalenceClass = useEquivalenceClass;
	}
}
