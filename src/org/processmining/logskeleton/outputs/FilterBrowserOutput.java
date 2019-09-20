package org.processmining.logskeleton.outputs;

import javax.swing.JComponent;

public class FilterBrowserOutput {

	private JComponent component;

	public FilterBrowserOutput(JComponent component) {
		this.component = component;
	}
	
	public JComponent getComponent() {
		return component;
	}
}
