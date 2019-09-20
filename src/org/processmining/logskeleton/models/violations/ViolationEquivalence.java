package org.processmining.logskeleton.models.violations;

import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.Violation;

public class ViolationEquivalence extends Violation {

	public ViolationEquivalence(XTrace trace, Set<String> violatingActivities) {
		super(trace, violatingActivities);
	}

	public String toString() {
		return "[ViolationEquivalence] Case " + XConceptExtension.instance().extractName(getTrace()) + ": Equivalence violated for "
				+ getViolatingActivities();
	}
}
