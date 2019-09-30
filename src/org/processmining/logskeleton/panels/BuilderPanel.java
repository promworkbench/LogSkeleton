package org.processmining.logskeleton.panels;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XLog;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.logskeleton.configurations.BuilderConfiguration;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class BuilderPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1040026869625729765L;

	public BuilderPanel(XLog log, final BuilderConfiguration configuration) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
		setLayout(new TableLayout(size));
		add(new ClassifierPanel(log.getClassifiers(), configuration), "0, 0");

		final NiceSlider horizonSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Horizon (0 means no horizon)", 0, 20, configuration.getHorizon(), Orientation.HORIZONTAL);
		horizonSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = horizonSlider.getSlider().getValue();
				configuration.setHorizon(value);
			}
		});
		horizonSlider.setPreferredSize(new Dimension(100, 30));
		add(horizonSlider, "0, 1");
	}
}
