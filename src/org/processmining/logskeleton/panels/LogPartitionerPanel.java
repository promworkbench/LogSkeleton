package org.processmining.logskeleton.panels;

import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.logskeleton.configurations.LogPartitionerConfiguration;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class LogPartitionerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4120395848381308094L;

	public LogPartitionerPanel(XLog log, LogPartitionerConfiguration configuration) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL, 30 } };
		setLayout(new TableLayout(size));
		add(new ClassifierPanel(log.getClassifiers(), configuration), "0, 0");

	}
}
