package org.processmining.logskeleton.algorithms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.util.CompositePanel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.logskeleton.classifiers.PrefixClassifier;
import org.processmining.logskeleton.configurations.FilterBrowserConfiguration;
import org.processmining.logskeleton.configurations.VisualizerConfiguration;
import org.processmining.logskeleton.inputs.FilterBrowserInput;
import org.processmining.logskeleton.inputs.VisualizerInput;
import org.processmining.logskeleton.outputs.VisualizerOutput;

import com.fluxicon.slickerbox.components.SlickerButton;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class VisualizerAlgorithm {

	private PluginContext context;
	private XLog log;
	private XEventClassifier classifier;
	private JComponent mainPanel;
	private JComponent bottomPanel = null;
	
	public VisualizerOutput apply(PluginContext context, VisualizerInput input, VisualizerConfiguration configuration) {
		this.context = context;
		this.log = input.getLog();
		
		mainPanel = new CompositePanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2466318591928364179L;

			public JComponent getMainComponent() {
				if (bottomPanel instanceof CompositePanel) {
					return ((CompositePanel) bottomPanel).getMainComponent();
				}
				return bottomPanel;
			}
		};
		double size[][] = { { TableLayoutConstants.FILL }, { 30, TableLayoutConstants.FILL } };
		mainPanel.setLayout(new TableLayout(size));
		mainPanel.setOpaque(false);

		classifier = new PrefixClassifier(configuration.getClassifier());

		mainPanel.add(getControlPanel(), "0, 0");

		update();

		return new VisualizerOutput(mainPanel);
	}
	
	private void update() {
		FilterBrowserAlgorithm filterBrowserAlgorithm = new FilterBrowserAlgorithm();
		if (bottomPanel != null) {
			mainPanel.remove(bottomPanel);
		}
		FilterBrowserInput input = new FilterBrowserInput(log);
		FilterBrowserConfiguration configuration = new FilterBrowserConfiguration(input);
		configuration.setClassifier(classifier);
		bottomPanel = filterBrowserAlgorithm.apply(context, input, configuration).getComponent();
		mainPanel.add(bottomPanel, "0, 1");
		mainPanel.validate();
		mainPanel.repaint();
	}
	
	private JComponent getControlPanel() {
		JPanel controlPanel = new JPanel();
		double size[][] = { { 250, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		controlPanel.setLayout(new TableLayout(size));
		controlPanel.setOpaque(false);
		
		String[] keys = classifier.getDefiningAttributeKeys();
		String text = "";
		String sep = "";
		for (int i = 0; i < keys.length; i++) {
			text += sep + keys[i];
			sep = " ";
		}
		final ProMTextField input = new ProMTextField(text);
		controlPanel.add(input, "1, 0");

		final SlickerButton button = new SlickerButton("Apply classifier \u25b6");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String keys[] = input.getText().split(" ");
				classifier = new PrefixClassifier(new XEventAttributeClassifier("classifier", keys));
				update();
			}

		});
		controlPanel.add(button, "0, 0");

		return controlPanel;
	}
}
