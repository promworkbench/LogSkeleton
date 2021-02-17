package org.processmining.logskeleton.models;

import java.util.HashMap;
import java.util.Map;

public class LogSkeletonNode {

	/*
	 * Maps activity to outgoing edge to that activity.
	 */
	private Map<LogSkeletonNode, LogSkeletonEdge> outgoing;
	
	/*
	 * Maps activity to incoming edge from that activity.
	 */
	private Map<LogSkeletonNode, LogSkeletonEdge> incoming;
	
	/*
	 * Activity label.
	 */
	private String label;
	
	/*
	 * Activity label of representative of equivalence class.
	 */
	private String labelRepresentative;
	
	/*
	 * Number of times this activity occurs in the log.
	 */
	private int count;
	
	/*
	 * Lowest number of times this activity occurs in any trace.
	 */
	private int low;

	/*
	 * Highest number of times this activity occurs in any trace.
	 */
	private int high;
	
	/*
	 * Whether the node was selected by the user.
	 */
	private boolean selected;
	
	public LogSkeletonNode() {
		setOutgoing(new HashMap<LogSkeletonNode, LogSkeletonEdge>());
		setIncoming(new HashMap<LogSkeletonNode, LogSkeletonEdge>());
	}
	
	/*
	 * Getters and setters.
	 */
	public Map<LogSkeletonNode, LogSkeletonEdge> getOutgoing() {
		return outgoing;
	}
	
	public void setOutgoing(Map<LogSkeletonNode, LogSkeletonEdge> outgoing) {
		this.outgoing = outgoing;
	}
	
	public Map<LogSkeletonNode, LogSkeletonEdge> getIncoming() {
		return incoming;
	}
	
	public void setIncoming(Map<LogSkeletonNode, LogSkeletonEdge> incoming) {
		this.incoming = incoming;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabelRepresentative() {
		return labelRepresentative;
	}

	public void setLabelRepresentative(String labelRepresentative) {
		this.labelRepresentative = labelRepresentative;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getLow() {
		return low;
	}

	public void setLow(int low) {
		this.low = low;
	}

	public int getHigh() {
		return high;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
