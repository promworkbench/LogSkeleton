package org.processmining.logskeleton.models.violations;

import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.Violation;

public class ViolationPrecedence extends Violation {
	
	private String activity;
	
	public ViolationPrecedence(XTrace trace, String activity, Set<String> violatingActivities) {
		super(trace, violatingActivities);
		this.activity = activity;
	}

	public String getActivity() {
		return activity;
	}

	public String toString() {
		return "[ViolationPrecedence] Case " + XConceptExtension.instance().extractName(getTrace()) + ": Precedence violated for " + activity + ", missing are "
				+ getViolatingActivities();
	}
}
