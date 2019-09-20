package org.processmining.logskeleton.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XLog;
import org.processmining.logskeleton.configurations.ClassifierConfiguration;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class ClassifierPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 676186087055650845L;

	public ClassifierPanel(XLog log, ClassifierConfiguration configuration) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
		setLayout(new TableLayout(size));
		add(new org.processmining.log.dialogs.ClassifierPanel(log.getClassifiers(), configuration), "0, 0");
		
		final JCheckBox check1 = SlickerFactory.instance().createCheckBox("Create a resource for every filtered log skeleton and log (on All tab)",
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

		final NiceSlider slider = SlickerFactory.instance().createNiceIntegerSlider("Maximal Filter Depth", 0, 3,
				configuration.getMaxFilterDepth(), Orientation.HORIZONTAL);
		slider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = slider.getSlider().getValue();
				configuration.setMaxFilterDepth(value);
			}
		});
		slider.setPreferredSize(new Dimension(100, 30));
		add(slider, "0, 2");
}
}
