package org.processmining.logskeleton.configurations;

import org.deckfour.xes.model.XLog;
import org.processmining.logskeleton.inputs.PartitionerInput;

/*
 * @deprecated Use PartitionerAlgorithm instead.
 */
@Deprecated
public class LogPartitionerConfiguration extends PartitionerConfiguration {

	public LogPartitionerConfiguration(XLog log) {
		super(new PartitionerInput(log));
	}
}
