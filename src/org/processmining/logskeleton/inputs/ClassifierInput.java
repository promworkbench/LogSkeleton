package org.processmining.logskeleton.inputs;

import org.deckfour.xes.model.XLog;

public class ClassifierInput {
	
	private XLog referenceLog;
	private XLog log;
	
	public ClassifierInput(XLog referenceLog, XLog log) {
		this.referenceLog = referenceLog;
		this.log = log;
	}

	public XLog getReferenceLog() {
		return referenceLog;
	}
	
	public XLog getLog() {
		return log;
	}
}
