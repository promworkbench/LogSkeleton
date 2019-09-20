package org.processmining.logskeleton.inputs;

import org.deckfour.xes.model.XLog;

public class VisualizerInput {

	private XLog log;

	public VisualizerInput(XLog log) {
		this.log = log;
	}

	public XLog getLog() {
		return log;
	}
}
