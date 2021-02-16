package org.processmining.logskeleton.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogSkeletonGraph {

	/*
	 * Nodes in the graph.
	 */
	private Set<LogSkeletonNode> nodes;
	
	/*
	 * Edges in the graph. 
	 */
	private Set<LogSkeletonEdge> edges;
	
	/*
	 * Graph title.
	 */
	private String title;
	
	/*
	 * Lines for the legend.
	 */
	private List<LogSkeletonLegendLine> legendLines;
	
	/*
	 * Default edge color.
	 */
	private String defaultEdgeColor;
	
	/*
	 * Log skeleton.
	 */
	private LogSkeleton logSkeleton;
	
	public LogSkeletonGraph() {
		setNodes(new HashSet<LogSkeletonNode>());
		setEdges(new HashSet<LogSkeletonEdge>());
		setLegendLines(new ArrayList<LogSkeletonLegendLine>());
	}

	public Set<LogSkeletonNode> getNodes() {
		return nodes;
	}

	/*
	 * Getters and setters.
	 */
	public void setNodes(Set<LogSkeletonNode> nodes) {
		this.nodes = nodes;
	}

	public Set<LogSkeletonEdge> getEdges() {
		return edges;
	}

	public void setEdges(Set<LogSkeletonEdge> edges) {
		this.edges = edges;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<LogSkeletonLegendLine> getLegendLines() {
		return legendLines;
	}

	public void setLegendLines(List<LogSkeletonLegendLine> legendLines) {
		this.legendLines = legendLines;
	}

	public String getDefaultEdgeColor() {
		return defaultEdgeColor;
	}

	public void setDefaultEdgeColor(String defaultEdgeColor) {
		this.defaultEdgeColor = defaultEdgeColor;
	}

	public LogSkeleton getLogSkeleton() {
		return logSkeleton;
	}

	public void setLogSkeleton(LogSkeleton logSkeleton) {
		this.logSkeleton = logSkeleton;
	}
}
