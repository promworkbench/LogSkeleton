package org.processmining.logskeleton.models;

public class LogSkeletonEdge {

	/*
	 * Source node.
	 */
	private LogSkeletonNode source;
	
	/*
	 * Target node.
	 */
	private LogSkeletonNode target;
	
	/*
	 * Head type.
	 */
	private LogSkeletonEdgeType head;
	
	/*
	 * Tail type.
	 */
	private LogSkeletonEdgeType tail;
	
	/*
	 * Head label.
	 */
	private String headLabel;
	
	/*
	 * Tail label.
	 */
	private String tailLabel;
	
	/*
	 * Head color.
	 */
	private String headColor;
	
	/*
	 * Tail color.
	 */
	private String tailColor;
	
	/*
	 * Whether symmetric.
	 */
	private boolean isSymmetric;

	/*
	 * Getters and setters.
	 */
	public LogSkeletonNode getSource() {
		return source;
	}

	public void setSource(LogSkeletonNode source) {
		this.source = source;
	}

	public LogSkeletonNode getTarget() {
		return target;
	}

	public void setTarget(LogSkeletonNode target) {
		this.target = target;
	}
	
	public LogSkeletonEdgeType getHead() {
		return head;
	}
	
	public void setHead(LogSkeletonEdgeType head) {
		this.head = head;
	}

	public LogSkeletonEdgeType getTail() {
		return tail;
	}

	public void setTail(LogSkeletonEdgeType tail) {
		this.tail = tail;
	}

	public String getHeadLabel() {
		return headLabel;
	}

	public void setHeadLabel(String headLabel) {
		this.headLabel = headLabel;
	}

	public String getTailLabel() {
		return tailLabel;
	}

	public void setTailLabel(String tailLabel) {
		this.tailLabel = tailLabel;
	}

	public String getHeadColor() {
		return headColor;
	}

	public void setHeadColor(String headColor) {
		this.headColor = headColor;
	}

	public String getTailColor() {
		return tailColor;
	}

	public void setTailColor(String tailColor) {
		this.tailColor = tailColor;
	}

	public boolean isSymmetric() {
		return isSymmetric;
	}

	public void setSymmetric(boolean isSymmetric) {
		this.isSymmetric = isSymmetric;
	}
}
