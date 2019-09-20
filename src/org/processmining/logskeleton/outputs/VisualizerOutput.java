package org.processmining.logskeleton.outputs;

import javax.swing.JComponent;

public class VisualizerOutput {

	private JComponent component;

	public VisualizerOutput(JComponent component) {
		this.component = component;
	}
	
	public JComponent getComponent() {
		return component;
	}
}
