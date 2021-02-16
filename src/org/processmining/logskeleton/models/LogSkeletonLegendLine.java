package org.processmining.logskeleton.models;

public class LogSkeletonLegendLine {

	private String label;
	private String value;
	
	public LogSkeletonLegendLine(String label, String value) {
		setLabel(label);
		setValue(value);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
