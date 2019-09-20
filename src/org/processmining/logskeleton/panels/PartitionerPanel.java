package org.processmining.logskeleton.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.logskeleton.configurations.PartitionerConfiguration;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class PartitionerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8303071009985037223L;

	public PartitionerPanel(XLog log, final PartitionerConfiguration configuration) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
		setLayout(new TableLayout(size));
		add(new ClassifierPanel(log.getClassifiers(), configuration), "0, 0");

		final JCheckBox check1 = SlickerFactory.instance().createCheckBox("Push every sublog into the workspace",
				false);
		check1.setSelected(configuration.isCreateProvidedObjects());
		check1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setCreateProvidedObjects(check1.isSelected());
			}

		});
		check1.setOpaque(false);
		check1.setPreferredSize(new Dimension(100, 30));
		add(check1, "0, 1");
	}
}
