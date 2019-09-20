package org.processmining.logskeleton.outputs;

import java.util.Collection;
import java.util.HashSet;

import org.deckfour.xes.model.XLog;
import org.processmining.logskeleton.models.Violation;

public class CheckerOutput {

	private XLog log;
	private Collection<Violation> violations;
	
	public CheckerOutput(XLog log) {
		this.log = log;
		violations = new HashSet<Violation>();
	}

	public XLog getLog() {
		return log;
	}

	public Collection<Violation> getViolations() {
		return violations;
	}
}
