package org.processmining.logskeleton.inputs;

import org.deckfour.xes.model.XLog;

public class BuilderInput {

	private XLog log;
	
	public BuilderInput(XLog log) {
		this.log = log;
	}

	public XLog getLog() {
		return log;
	}
}
