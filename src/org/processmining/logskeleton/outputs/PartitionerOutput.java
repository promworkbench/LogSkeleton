package org.processmining.logskeleton.outputs;

import org.processmining.log.models.EventLogArray;

public class PartitionerOutput {

	private EventLogArray logs;

	public PartitionerOutput(EventLogArray logs) {
		this.logs = logs;
	}

	public EventLogArray getLogs() {
		return logs;
	}
}
