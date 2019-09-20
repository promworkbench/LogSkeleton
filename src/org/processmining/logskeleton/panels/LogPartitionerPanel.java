package org.processmining.logskeleton.panels;

import org.deckfour.xes.model.XLog;
import org.processmining.logskeleton.configurations.PartitionerConfiguration;

/*
 * @deprecated Use PartitionerPanel instead.
 */
@Deprecated
public class LogPartitionerPanel extends PartitionerPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4120395848381308094L;

	public LogPartitionerPanel(XLog log, PartitionerConfiguration configuration) {
		super(log, configuration);
	}
}
