package org.processmining.logskeleton.models;

public class LogSkeletonEdge {

	/*
	 * Head node.
	 */
	private LogSkeletonNode headNode;
	
	/*
	 * Tail node.
	 */
	private LogSkeletonNode tailNode;
	
	/*
	 * Head type.
	 */
	private LogSkeletonEdgeType headType;
	
	/*
	 * Tail type.
	 */
	private LogSkeletonEdgeType tailType;
	
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
	public LogSkeletonNode getTailNode() {
		return tailNode;
	}

	public void setTailNode(LogSkeletonNode source) {
		this.tailNode = source;
	}

	public LogSkeletonNode getHeadNode() {
		return headNode;
	}

	public void setHeadNode(LogSkeletonNode target) {
		this.headNode = target;
	}
	
	public LogSkeletonEdgeType getHeadType() {
		return headType;
	}
	
	public void setHeadType(LogSkeletonEdgeType head) {
		this.headType = head;
	}

	public LogSkeletonEdgeType getTailType() {
		return tailType;
	}

	public void setTailType(LogSkeletonEdgeType tail) {
		this.tailType = tail;
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
