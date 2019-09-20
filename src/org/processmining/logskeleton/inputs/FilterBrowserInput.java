package org.processmining.logskeleton.inputs;

import org.deckfour.xes.model.XLog;

public class FilterBrowserInput {

	private XLog log;

	public FilterBrowserInput(XLog log) {
		this.log = log;
	}

	public XLog getLog() {
		return log;
	}
}
