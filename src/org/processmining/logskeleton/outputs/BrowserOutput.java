package org.processmining.logskeleton.outputs;

import javax.swing.JComponent;

public class BrowserOutput {

	private JComponent component;

	public BrowserOutput(JComponent component) {
		this.component = component;
	}
	
	public JComponent getComponent() {
		return component;
	}
}
