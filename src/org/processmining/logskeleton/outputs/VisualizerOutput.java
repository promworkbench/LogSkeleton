package org.processmining.logskeleton.outputs;

import org.processmining.logskeleton.components.VisualizerComponent;

public class VisualizerOutput {

	private VisualizerComponent component;

	public VisualizerOutput(VisualizerComponent component) {
		this.component = component;
	}
	
	public VisualizerComponent getComponent() {
		return component;
	}
}
