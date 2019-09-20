package org.processmining.logskeleton.inputs;

import org.deckfour.xes.model.XLog;
import org.processmining.logskeleton.models.LogSkeleton;

public class CheckerInput {

	private LogSkeleton logSkeleton;	
	private XLog log;

	public CheckerInput(LogSkeleton logSkeleton, XLog log) {
		this.logSkeleton = logSkeleton;
		this.log = log;
	}

	public LogSkeleton getLogSkeleton() {
		return logSkeleton;
	}

	public XLog getLog() {
		return log;
	}
}
