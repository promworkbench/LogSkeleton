package org.processmining.logskeleton.models.violations;

import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.Violation;

public class ViolationCardinality extends Violation {

	public ViolationCardinality(XTrace trace, Set<String> violatingActivities) {
		super(trace, violatingActivities);
	}
	
	public String toString() {
		return "[ViolationCardinality] Case " + XConceptExtension.instance().extractName(getTrace()) + ": cardinality violated for " + getViolatingActivities();
	}
}
