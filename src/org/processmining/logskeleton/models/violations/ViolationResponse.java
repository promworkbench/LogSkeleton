package org.processmining.logskeleton.models.violations;

import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.Violation;

public class ViolationResponse extends Violation {

	private String activity;
	
	public ViolationResponse(XTrace trace, String activity, Set<String> violatingActivities) {
		super(trace, violatingActivities);
		this.activity = activity;
	}

	public String getActivity() {
		return activity;
	}

	public String toString() {
		return "[ViolationResponse] Case " + XConceptExtension.instance().extractName(getTrace()) + ": Response violated for " + activity + ", missing are "
				+ getViolatingActivities();
	}
}
