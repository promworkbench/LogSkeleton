package org.processmining.logskeleton.algorithms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonEdge;
import org.processmining.logskeleton.models.LogSkeletonEdgeType;
import org.processmining.logskeleton.models.LogSkeletonGraph;
import org.processmining.logskeleton.models.LogSkeletonLegendLine;
import org.processmining.logskeleton.models.LogSkeletonNode;
import org.processmining.logskeleton.models.LogSkeletonRelation;

public class GraphBuilderAlgorithm {

	/*
	 * Colors to use.
	 */
	private String[] colors;
	
	/*
	 * Index of next free color.
	 */
	private int colorIndex;
	
	/*
	 * Map from representatives to colors.
	 */
	private Map<String, String> colorMap;
	
	/*
	 * Set of activities in the graph.
	 */
	private Set<String> activities;

	/*
	 * The graph.
	 */
	private LogSkeletonGraph graph;

	/*
	 * Map from activities to nodes.
	 */
	private Map<String, LogSkeletonNode> nodeMap;

	/*
	 * For edges without a relation.
	 */
	private String defaultColor;
	/*
	 * For edges with a Non Co-Existence relation.
	 */
	private String lighterNotCoExistenceColor;
	private String notCoExistenceColor;
	/*
	 * For edges with a Response or Precedence relation.
	 */
	private String lighterResponsePrecedenceColor;
	private String responsePrecedenceColor;
	/*
	 * For edges with a Not Response or Not Precedence relation.
	 */
	private String lighterNotResponsePrecedenceColor;
	private String notResponsePrecedenceColor;

	public LogSkeletonGraph apply(LogSkeleton logSkeleton, BrowserConfiguration configuration) {
		graph = new LogSkeletonGraph();

		initializeColors();

		/*
		 * Initialization
		 */
		colorIndex = 0;
		colorMap = new HashMap<String, String>();

		/*
		 * Get all selected activities. Retain only those that are actually present.
		 */
		activities = new HashSet<String>(configuration.getActivities());
		activities.retainAll(logSkeleton.getActivities());

		/*
		 * Copy the thresholds.
		 */
		logSkeleton.setPrecedenceThreshold(configuration.getPrecedenceThreshold());
		logSkeleton.setResponseThreshold(configuration.getResponseThreshold());
		logSkeleton.setNotCoExistenceThreshold(configuration.getNotCoExistenceThreshold());
		logSkeleton.setEquivalenceThreshold(configuration.getEquivalenceThreshold());

		/*
		 * Add related but unselected activities, if needed.
		 */
		addNeighbors(logSkeleton, configuration);

		/*
		 * Add a node for every activity.
		 */
		addNodes(logSkeleton, configuration);

		/*
		 * Add an edge between two activities if there is a relation between both activities.
		 */
		addEdges(logSkeleton, configuration);

		/*
		 * Add the legend.
		 */
		addLegend(logSkeleton, configuration);

		return graph;
	}

	private void initializeColors() {
		/*
		 * Create a color scheme (based on Set312) containing 99 different colors
		 * (including gradients). Color 100 is white and is used as fallback color.
		 */
		String[] set312Colors = new String[] { "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462",
				"#b3de69", "#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f" };
		colors = new String[100];
		for (int i = 0; i < 99; i++) {
			int m = i / 12;
			int d = i % 12;
			if (m == 0) {
				// Basic color, no gradient.
				colors[i] = set312Colors[i];
			} else {
				// Extended color, gradient.
				colors[i] = set312Colors[d] + ":" + set312Colors[(d + m) % 12];
			}
		}
		// Fall-back color
		colors[99] = "white";

		/*
		 * For edges without a relation.
		 */
		defaultColor = darker("#d9d9d9");
		/*
		 * For edges with a Non Co-Existence relation.
		 */
		lighterNotCoExistenceColor = "#fdb462";
		notCoExistenceColor = darker(lighterNotCoExistenceColor);
		/*
		 * For edges with a Response or Precedence relation.
		 */
		lighterResponsePrecedenceColor = "#80b1d3";
		responsePrecedenceColor = darker(lighterResponsePrecedenceColor);
		/*
		 * For edges with a Not Response or Not Precedence relation.
		 */
		lighterNotResponsePrecedenceColor = "#fb8072";
		notResponsePrecedenceColor = darker(lighterNotResponsePrecedenceColor);
}

	private void addNeighbors(LogSkeleton logSkeleton, BrowserConfiguration configuration) {
		if (configuration.isUseNeighbors()) {
			/*
			 * Extend the selected activities with activities that are related (through some
			 * selected non-redundant relation) to a selected activity.
			 */
			for (String tailActivity : logSkeleton.getActivities()) {
				for (String headActivity : logSkeleton.getActivities()) {
					if ((configuration.getActivities().contains(tailActivity)
							|| configuration.getActivities().contains(headActivity))
							&& (!activities.contains(tailActivity) || !activities.contains(headActivity))) {
						if (configuration.getRelations().contains(LogSkeletonRelation.RESPONSE)) {
							if (logSkeleton.hasNonRedundantResponse(tailActivity, headActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Response relation, and one of them is selected. Include the other as well.
								 */
								activities.add(tailActivity);
								activities.add(headActivity);
								continue;
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.PRECEDENCE)) {
							if (logSkeleton.hasNonRedundantPrecedence(tailActivity, headActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Precedence relation, and one of them is selected. Include the other as well.
								 */
								activities.add(tailActivity);
								activities.add(headActivity);
								continue;
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.NOTRESPONSE)) {
							if (logSkeleton.hasNonRedundantNotResponse(tailActivity, headActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Not Response relation, and one of them is selected. Include the other as
								 * well.
								 */
								activities.add(tailActivity);
								activities.add(headActivity);
								continue;
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.NOTPRECEDENCE)) {
							if (logSkeleton.hasNonRedundantNotPrecedence(tailActivity, headActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Not Precedence relation, and one of them is selected. Include the other as
								 * well.
								 */
								activities.add(tailActivity);
								activities.add(headActivity);
								continue;
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.NOTCOEXISTENCE)) {
							if (logSkeleton.hasNonRedundantNotCoExistence(tailActivity, headActivity, configuration)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Not Co-Existence relation, and one of them is selected. Include the other as
								 * well.
								 */
								activities.add(tailActivity);
								activities.add(headActivity);
								continue;
							}
						}
					}
				}
			}
		}

	}

	private void addNodes(LogSkeleton logSkeleton, BrowserConfiguration configuration) {
		nodeMap = new HashMap<String, LogSkeletonNode>();
		/*
		 * Create a dot node for every (included) activity.
		 */
		for (String activity : activities) {
			LogSkeletonNode node = new LogSkeletonNode();
			node.setLabel(activity);

			/*
			 * Get a color for this activity.
			 */
			String representative = logSkeleton.getEquivalenceClass(activity, activities).iterator().next();
			node.setLabelRepresentative(representative);
			String representativeColor = colorMap.get(representative);
			if (representativeColor == null) {
				representativeColor = colors[colorIndex];
				colorMap.put(representative, representativeColor);
				if (colorIndex < colors.length - 1) {
					colorIndex++;
				}
			}
			node.setBackgroundColor(representativeColor);

			/*
			 * Get the count for this activity.
			 */
			node.setCount(logSkeleton.getCount(activity));

			/*
			 * Get the count interval for this activity.
			 */
			node.setLow(logSkeleton.getMin(activity));
			node.setHigh(logSkeleton.getMax(activity));

			/*
			 * Determine the border width for this activity: 
			 * - 1 if selected 
			 * - 0 if not selected (but included).
			 * 
			 */
			node.setBorder(configuration.getActivities().contains(activity));

			nodeMap.put(activity, node);

			graph.getNodes().add(node);
		}
	}

	private void addEdges(LogSkeleton logSkeleton, BrowserConfiguration configuration) {

		for (String tailActivity : activities) {
			for (String headActivity : activities) {
				if (configuration.getActivities().contains(tailActivity)
						|| configuration.getActivities().contains(headActivity)) {
					LogSkeletonEdge edge = new LogSkeletonEdge();
					LogSkeletonNode headNode = nodeMap.get(tailActivity);
					LogSkeletonNode tailNode = nodeMap.get(headActivity);
					edge.setTailNode(headNode);
					edge.setHeadNode(tailNode);
					edge.setHeadColor(defaultColor);
					edge.setTailColor(defaultColor);
					if (configuration.getRelations().contains(LogSkeletonRelation.RESPONSE)) {
						if (edge.getTailType() == null && logSkeleton.hasNonRedundantResponse(tailActivity, headActivity)) {
							/*
							 * Add Response on tail.
							 */
							edge.setTailType(LogSkeletonEdgeType.ALWAYS);
							edge.setSymmetric(false);
							int threshold = logSkeleton.getMaxThresholdResponse(tailActivity, headActivity);
							if (threshold < 100) {
								edge.setTailLabel("." + threshold);
								edge.setTailColor(lighterResponsePrecedenceColor);
							} else {
								edge.setTailLabel(null);
								edge.setTailColor(responsePrecedenceColor);
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.PRECEDENCE)) {
						if (edge.getHeadType() == null && logSkeleton.hasNonRedundantPrecedence(tailActivity, headActivity)) {
							/*
							 * Add Precedence on head.
							 */
							edge.setHeadType(LogSkeletonEdgeType.ALWAYS);
							edge.setSymmetric(false);
							int threshold = logSkeleton.getMaxThresholdPrecedence(tailActivity, headActivity);
							if (threshold < 100) {
								edge.setHeadLabel("." + threshold);
								edge.setHeadColor(lighterResponsePrecedenceColor);
							} else {
								edge.setHeadLabel(null);
								;
								edge.setHeadColor(responsePrecedenceColor);
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.NOTCOEXISTENCE)) {
						if (!tailActivity.equals(headActivity)) {
							if (edge.getHeadType() == null && tailActivity.compareTo(headActivity) >= 0 && logSkeleton
									.hasNonRedundantNotCoExistence(headActivity, tailActivity, configuration)) {
								boolean doShow = configuration.isUseNCEReductions() ? logSkeleton.showNotCoExistence(
										tailActivity, headActivity, activities, configuration.getActivities()) : true;
								if (doShow) {
									/*
									 * Add Not Co-Existence on head.
									 */
									edge.setHeadType(LogSkeletonEdgeType.EXCLUSIVE);
									edge.setSymmetric(true);
									int threshold = logSkeleton.getMaxThresholdNotCoExistence(headActivity, tailActivity);
									if (threshold < 100) {
										edge.setHeadLabel("." + threshold);
										edge.setHeadColor(lighterNotCoExistenceColor);
									} else {
										edge.setHeadLabel(null);
										edge.setHeadColor(notCoExistenceColor);
									}
								}
							}
							if (edge.getTailType() == null && tailActivity.compareTo(headActivity) >= 0 && logSkeleton
									.hasNonRedundantNotCoExistence(tailActivity, headActivity, configuration)) {
								boolean doShow = configuration.isUseNCEReductions() ? logSkeleton.showNotCoExistence(
										tailActivity, headActivity, activities, configuration.getActivities()) : true;
								if (doShow) {
									/*
									 * Add Not Co-Existence on tail.
									 */
									edge.setTailType(LogSkeletonEdgeType.EXCLUSIVE);
									edge.setSymmetric(true);
									int threshold = logSkeleton.getMaxThresholdNotCoExistence(tailActivity, headActivity);
									if (threshold < 100) {
										edge.setTailLabel("." + threshold);
										edge.setTailColor(lighterNotCoExistenceColor);
									} else {
										edge.setTailLabel(null);
										edge.setTailColor(notCoExistenceColor);
									}
								}
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.NOTRESPONSE)) {
						if (!tailActivity.equals(headActivity) && edge.getHeadType() == null
								&& logSkeleton.hasNonRedundantNotResponse(tailActivity, headActivity)) {
							/*
							 * Add Not Response on head.
							 */
							edge.setHeadType(LogSkeletonEdgeType.NEVER);
							edge.setSymmetric(false);
							int threshold = logSkeleton.getMaxThresholdNotResponse(tailActivity, headActivity);
							if (threshold < 100) {
								edge.setHeadLabel("." + threshold);
								edge.setHeadColor(lighterNotResponsePrecedenceColor);
							} else {
								edge.setHeadLabel(null);
								edge.setHeadColor(notResponsePrecedenceColor);
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.NOTPRECEDENCE)) {
						if (!tailActivity.equals(headActivity) && edge.getTailType() == null
								&& logSkeleton.hasNonRedundantNotPrecedence(tailActivity, headActivity)) {
							/*
							 * Add Not Precedence on tail.
							 */
							edge.setTailType(LogSkeletonEdgeType.NEVER);
							edge.setSymmetric(false);
							int threshold = logSkeleton.getMaxThresholdNotPrecedence(tailActivity, headActivity);
							if (threshold < 100) {
								edge.setTailLabel("." + threshold);
								edge.setTailColor(lighterNotResponsePrecedenceColor);
							} else {
								edge.setTailLabel(null);
								edge.setTailColor(notResponsePrecedenceColor);
							}
						}
					}
					if (edge.getHeadType() != null || edge.getTailType() != null) {
						/*
						 * Some non-redundant relation found, add to graph.
						 */
						List<LogSkeletonNode> nodes = new ArrayList<LogSkeletonNode>();
						nodes.add(headNode);
						nodes.add(tailNode);
						graph.getEdges().put(nodes, edge);
						headNode.getOutgoing().put(tailNode, edge);
						tailNode.getIncoming().put(headNode, edge);
					}
				}
			}
		}
	}

	private void addLegend(LogSkeleton logSkeleton, BrowserConfiguration configuration) {
		List<String> selectedActivities = new ArrayList<String>(configuration.getActivities());
		Collections.sort(selectedActivities);
		/*
		 * Show name of log skeleton (that is, the name of the log).
		 */
		graph.getLegendLines().add(new LogSkeletonLegendLine("Event Log",
				logSkeleton.getLabel() == null ? "<not specified>" : logSkeleton.getLabel()));
		if (!logSkeleton.getRequired().isEmpty()) {
			/*
			 * List required activities.
			 */
			graph.getLegendLines()
					.add(new LogSkeletonLegendLine("Required activities", logSkeleton.getRequired().toString()));
		}
		if (!logSkeleton.getForbidden().isEmpty()) {
			/*
			 * List forbidden activities
			 */
			graph.getLegendLines()
					.add(new LogSkeletonLegendLine("Forbidden activities", logSkeleton.getForbidden().toString()));
		}
		if (!logSkeleton.getBoundary().isEmpty()) {
			/*
			 * List boundary activities
			 */
			graph.getLegendLines()
					.add(new LogSkeletonLegendLine("Boundary activities", logSkeleton.getBoundary().toString()));
		}
		if (!logSkeleton.getSplitters().isEmpty()) {
			/*
			 * List splitters.
			 */
			graph.getLegendLines().add(new LogSkeletonLegendLine("Splitters", logSkeleton.getSplitters().toString()));
		}
		/*
		 * List selected activities.
		 */
		graph.getLegendLines()
				.add(new LogSkeletonLegendLine("View activities", configuration.getActivities().toString()));
		/*
		 * List selected relations.
		 */
		graph.getLegendLines()
				.add(new LogSkeletonLegendLine("View relations", configuration.getRelations().toString()));
		/*
		 * Show horizon if not 0.
		 */
		if (logSkeleton.getHorizon() > 0) {
			graph.getLegendLines().add(new LogSkeletonLegendLine("Horizon", "" + logSkeleton.getHorizon()));
		}
		/*
		 * List noise levels for thresholds which are not set to 100.
		 */
		if (logSkeleton.getEquivalenceThreshold() < 100 || logSkeleton.getResponseThreshold() < 100
				|| logSkeleton.getPrecedenceThreshold() < 100 || logSkeleton.getNotCoExistenceThreshold() < 100) {
			String s = "";
			String d = "";
			if (logSkeleton.getEquivalenceThreshold() < 100) {
				s += d + "Equivalence = " + (100 - logSkeleton.getEquivalenceThreshold()) + "%";
				d = ", ";
			}
			if (logSkeleton.getResponseThreshold() < 100) {
				s += d + "(Not) Response = " + (100 - logSkeleton.getResponseThreshold()) + "%";
				d = ", ";
			}
			if (logSkeleton.getPrecedenceThreshold() < 100) {
				s += d + "(Not) Precedence = " + (100 - logSkeleton.getPrecedenceThreshold()) + "%";
				d = ", ";
			}
			if (logSkeleton.getNotCoExistenceThreshold() < 100) {
				s += d + "Not Co-Existence = " + (100 - logSkeleton.getNotCoExistenceThreshold()) + "%";
				d = ", ";
			}
			graph.getLegendLines().add(new LogSkeletonLegendLine("Noise levels", s));
		}
	}

	/*
	 * Returns a color which is slightly darker than the provided color.
	 */
	private String darker(String color) {
		Color darkerColor = Color.decode(color).darker();
		return "#" + Integer.toHexString(darkerColor.getRed()) + Integer.toHexString(darkerColor.getGreen())
				+ Integer.toHexString(darkerColor.getBlue());
	}

}
