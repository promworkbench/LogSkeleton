package org.processmining.logskeleton.inputs;

import org.deckfour.xes.model.XLog;

public class PartitionerInput {

	private XLog log;
	
	public PartitionerInput(XLog log) {
		this.log = log;
	}

	public XLog getLog() {
		return log;
	}
}
