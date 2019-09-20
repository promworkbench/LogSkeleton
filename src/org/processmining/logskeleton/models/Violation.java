package org.processmining.logskeleton.models;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XTrace;

public abstract class Violation {

	private XTrace trace;
	private Set<String> violatingActivities;
	
	public Violation(XTrace trace, Set<String> violatingActivities) {
		this.trace = trace;
		this.violatingActivities = new HashSet<String>(violatingActivities);
	}
	
	public XTrace getTrace() {
		return trace;
	}

	public Set<String> getViolatingActivities() {
		return violatingActivities;
	}	
	
}
