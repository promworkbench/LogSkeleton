package org.processmining.logskeleton.outputs;

import org.processmining.logskeleton.models.LogSkeleton;

public class BuilderOutput {

	private LogSkeleton logSkeleton;

	public BuilderOutput(LogSkeleton logSkeleton) {
		this.logSkeleton = logSkeleton;
	}

	public LogSkeleton getLogSkeleton() {
		return logSkeleton;
	}
}
