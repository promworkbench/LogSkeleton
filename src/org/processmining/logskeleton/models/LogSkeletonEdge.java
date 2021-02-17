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
	 * Head percentage.
	 */
	private int headPercentage;
	
	/*
	 * Tail percentage.
	 */
	private int tailPercentage;
	
	/*
	 * Whether symmetric.
	 */
	private boolean isSymmetric;

	public LogSkeletonEdge() {
		setHeadPercentage(100);
		setTailPercentage(100);
		setSymmetric(false);
	}
	
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

	public int getHeadPercentage() {
		return headPercentage;
	}

	public void setHeadPercentage(int headPercentage) {
		this.headPercentage = headPercentage;
	}

	public int getTailPercentage() {
		return tailPercentage;
	}

	public void setTailPercentage(int tailPercentage) {
		this.tailPercentage = tailPercentage;
	}

	public boolean isSymmetric() {
		return isSymmetric;
	}

	public void setSymmetric(boolean isSymmetric) {
		this.isSymmetric = isSymmetric;
	}
}
