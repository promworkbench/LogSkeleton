package org.processmining.logskeleton.models.violations;

import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.Violation;

public class ViolationNotResponse extends Violation {

	private String activity;
	
	public ViolationNotResponse(XTrace trace, String activity, Set<String> violatingActivities) {
		super(trace, violatingActivities);
		this.activity = activity;
	}

	public String getActivity() {
		return activity;
	}

	public String toString() {
		return "[ViolationNotResponse] Case " + XConceptExtension.instance().extractName(getTrace()) + ": Not Response violated for " + activity + ", present are "
				+ getViolatingActivities();
	}
}
