package org.processmining.logskeleton.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogSkeletonGraph {

	/*
	 * Nodes in the graph.
	 */
	private Set<LogSkeletonNode> nodes;
	
	/*
	 * Edges in the graph. 
	 * The list of nodes contains first the tail node and second the head node. 
	 * This, way, we prevent multiple edges from the source node to the target node.
	 */
	private Map<List<LogSkeletonNode>, LogSkeletonEdge> edges;
	
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
		setEdges(new HashMap<List<LogSkeletonNode>, LogSkeletonEdge>());
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

	public Map<List<LogSkeletonNode>, LogSkeletonEdge> getEdges() {
		return edges;
	}

	public void setEdges(Map<List<LogSkeletonNode>, LogSkeletonEdge> edges) {
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
