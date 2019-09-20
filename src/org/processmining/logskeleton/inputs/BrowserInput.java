package org.processmining.logskeleton.inputs;

import org.processmining.logskeleton.models.LogSkeleton;

public class BrowserInput {

	private LogSkeleton logSkeleton;

	public BrowserInput(LogSkeleton logSkeleton) {
		this.logSkeleton = logSkeleton;
	}
	
	public LogSkeleton getLogSkeleton() {
		return logSkeleton;
	}
}
