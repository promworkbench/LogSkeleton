package org.processmining.logskeleton.outputs;

import org.deckfour.xes.model.XLog;

public class ClassifierOutput {

	private XLog log;
	
	public ClassifierOutput(XLog log) {
		this.log = log;
	}

	public XLog getLog() {
		return log;
	}
}
