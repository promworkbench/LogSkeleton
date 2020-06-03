package org.processmining.logskeleton.outputs;

import org.processmining.logskeleton.components.BrowserComponent;

public class BrowserOutput {

	private BrowserComponent component;

	public BrowserOutput(BrowserComponent component) {
		this.component = component;
	}
	
	public BrowserComponent getComponent() {
		return component;
	}
}
