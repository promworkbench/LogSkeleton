package org.processmining.logskeleton.panels;

import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.logskeleton.configurations.BuilderConfiguration;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class BuilderPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1040026869625729765L;

	public BuilderPanel(XLog log, final BuilderConfiguration configuration) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		add(new ClassifierPanel(log.getClassifiers(), configuration), "0, 0");
	}
}
