package org.processmining.logskeleton.algorithms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.models.LogSkeletonEdge;
import org.processmining.logskeleton.models.LogSkeletonGraph;
import org.processmining.logskeleton.models.LogSkeletonLegendLine;
import org.processmining.logskeleton.models.LogSkeletonNode;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

public class GraphVisualizerAlgorithm {

	private Map<LogSkeletonNode, DotNode> map;
	private Dot dotGraph;

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

	public Dot apply(LogSkeletonGraph graph, BrowserConfiguration configuration) {
		map = new HashMap<LogSkeletonNode, DotNode>();
		dotGraph = new Dot();

		/*
		 * Initialize the colors.
		 */
		initializeColors();
		
		/*
		 * Add the nodes to Dot.
		 */
		addNodes(graph, configuration);

		/*
		 * Add the edges to Dot.
		 */
		addEdges(graph, configuration);

		/*
		 * Replaces cliques of edges in Dot by an hyper arc (if possible).
		 */
		addHyperArcs(graph, configuration);

		/*
		 * Add legend to Dot.
		 */
		addLegend(graph, configuration);

		return dotGraph;
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

		colorMap = new HashMap<String, String>();
		
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

	private void addNodes(LogSkeletonGraph graph, BrowserConfiguration configuration) {
		List<LogSkeletonNode> nodes = new ArrayList<LogSkeletonNode>(graph.getNodes());
		Collections.sort(nodes, new Comparator<LogSkeletonNode>() {

			public int compare(LogSkeletonNode arg0, LogSkeletonNode arg1) {
				// TODO Auto-generated method stub
				return arg0.getLabel().compareTo(arg1.getLabel());
			}
			
		});
		for (LogSkeletonNode node : nodes) {
			/*
			 * Get a color for this activity.
			 */
			String representative = node.getLabelRepresentative();
			String representativeColor = colorMap.get(representative);
			if (representativeColor == null) {
				representativeColor = colors[colorIndex];
				colorMap.put(representative, representativeColor);
				if (colorIndex < colors.length - 1) {
					colorIndex++;
				}
			}
			DotNode dotNode = dotGraph.addNode("<<table align=\"center\" bgcolor=\"" + representativeColor
					+ "\" border=\"" + (node.isSelected() ? "1" : "0")
					+ "\" cellborder=\"0\" cellpadding=\"2\" columns=\"*\" style=\"rounded\"><tr><td colspan=\"3\"><font point-size=\"24\"><b>"
					+ encodeHTML(node.getLabel()) + "</b></font></td></tr><hr/><tr><td>"
					+ encodeHTML(node.getLabelRepresentative()) + "</td><td>" + node.getCount() + "</td>" + "<td>"
					+ node.getLow() + (node.getLow() == node.getHigh() ? "" : ".." + node.getHigh()) + "</td>"
					+ "</tr></table>>");
			dotNode.setOption("shape", "none");
			if (!configuration.getFontname().isEmpty()) {
				dotNode.setOption("fontname", configuration.getFontname());
			}
			map.put(node, dotNode);
		}
	}

	private void addEdges(LogSkeletonGraph graph, BrowserConfiguration configuration) {
		List<LogSkeletonEdge> edges = new ArrayList(graph.getEdges().values());
		Collections.sort(edges, new Comparator<LogSkeletonEdge>() {

			public int compare(LogSkeletonEdge o1, LogSkeletonEdge o2) {
				// TODO Auto-generated method stub
				int c =  o1.getTailNode().getLabel().compareTo(o2.getTailNode().getLabel());
				if (c != 0) {
					return c;
				}
				return o1.getHeadNode().getLabel().compareTo(o2.getHeadNode().getLabel());
			}
			
		});
		for (LogSkeletonEdge edge : edges) {
			DotEdge dotEdge = dotGraph.addEdge(map.get(edge.getTailNode()), map.get(edge.getHeadNode()));
			dotEdge.setOption("dir", "both");
			String headDecorator = "";
			String tailDecorator = "";
			String headColor = defaultColor;
			String tailColor = defaultColor;
			if (edge.getHeadType() != null) {
				switch (edge.getHeadType()) {
					case ALWAYS : {
						headDecorator = "normal";
						if (edge.getHeadPercentage() == 100) {
							headColor = responsePrecedenceColor;
						} else {
							headColor = lighterResponsePrecedenceColor;
						}
						break;
					}
					case NEVER : {
						if (configuration.isUseInvertedArrows()) {
							headDecorator = "noneinvtee";
						} else {
							headDecorator = "onormal";
						}
						if (edge.getHeadPercentage() == 100) {
							headColor = notResponsePrecedenceColor;
						} else {
							headColor = lighterNotResponsePrecedenceColor;
						}
						break;
					}
					case EXCLUSIVE : {
						headDecorator = "nonetee";
						if (edge.getHeadPercentage() == 100) {
							headColor = notCoExistenceColor;
						} else {
							headColor = lighterNotCoExistenceColor;
						}
						break;
					}
				}
			}
			if (edge.getTailType() != null) {
				switch (edge.getTailType()) {
					case ALWAYS : {
						tailDecorator = "noneinv";
						if (edge.getTailPercentage() == 100) {
							tailColor = responsePrecedenceColor;
						} else {
							tailColor = lighterResponsePrecedenceColor;
						}
						break;
					}
					case NEVER : {
						if (configuration.isUseInvertedArrows()) {
							tailDecorator = "teenormal";
						} else {
							tailDecorator = "noneoinv";
						}
						if (edge.getTailPercentage() == 100) {
							tailColor = notResponsePrecedenceColor;
						} else {
							tailColor = lighterNotResponsePrecedenceColor;
						}
						break;
					}
					case EXCLUSIVE : {
						tailDecorator = "nonetee";
						if (edge.getTailPercentage() == 100) {
							tailColor = notCoExistenceColor;
						} else {
							tailColor = lighterNotCoExistenceColor;
						}
						break;
					}
				}
			}
			dotEdge.setOption("arrowtail", tailDecorator + "none");
			dotEdge.setOption("arrowhead", headDecorator + "none");
			if (!configuration.getFontname().isEmpty()) {
				dotEdge.setOption("fontname", configuration.getFontname());
			}

			if (configuration.isUseFalseConstraints() && edge.isSymmetric()) {
				/*
				 * Ignore symmetric relations in the layout.
				 */
				dotEdge.setOption("constraint", "false");
			}

			if (configuration.isUseEdgeColors()) {
				/*
				 * Color the edges.
				 */
				String color = tailColor + ";0.5:" + headColor + ";0.5";
				dotEdge.setOption("color", color);
			}

			if (configuration.isUseHeadTailLabels()) {
				/*
				 * Show labels seperatley at head/tail
				 */
				if (edge.getHeadPercentage() != 100) {
					dotEdge.setOption("headlabel", "." + edge.getHeadPercentage());
				}
				if (edge.getTailPercentage() != 100) {
					dotEdge.setOption("taillabel", "." + edge.getTailPercentage());
				}
			} else if (edge.getHeadPercentage() != 100 || edge.getTailPercentage() != 100) {
				/*
				 * Show labels combined at middle of arc
				 */
				String label = "";
				if (edge.getTailPercentage() != 100) {
					label += "." + edge.getTailPercentage();
				}
				label += "&rarr;";
				if (edge.getHeadPercentage() != 100) {
					label += "." + edge.getHeadPercentage();
				}
				dotEdge.setLabel(label);
			}
		}
	}

	private void addHyperArcs(LogSkeletonGraph graph, BrowserConfiguration configuration) {

		if (configuration.isUseHyperArcs()) {
			/*
			 * Replaces cliques of edges by a hyper edge.
			 */

			/*
			 * Sort the edge to get a (more) deterministic result.
			 */
			List<DotEdge> candidateEdges = new ArrayList<DotEdge>(dotGraph.getEdges());
			Collections.sort(candidateEdges, new Comparator<DotEdge>() {

				public int compare(DotEdge o1, DotEdge o2) {
					int c = o1.getSource().getLabel().compareTo(o2.getSource().getLabel());
					if (c == 0) {
						c = o1.getTarget().getLabel().compareTo(o2.getTarget().getLabel());
					}
					return c;
				}

			});

			/*
			 * Iterate over all edges in the (current!) graph.
			 * 
			 * Note that the graph may change in the process.
			 */
			while (!candidateEdges.isEmpty()) {
				/*
				 * Get the next edge.
				 */
				DotEdge edge = candidateEdges.iterator().next();
				/*
				 * For now, only do this for always-edges. Includes always-not (not response,
				 * not precedence) edges.
				 */
				if (edge.getOption("arrowtail").contains("inv") || edge.getOption("arrowhead").contains("inv")
						|| edge.getOption("arrowtail").contains("inv")
						|| edge.getOption("arrowhead").contains("normal")) {
					/*
					 * Get the cluster for this edge.
					 */
					DotNode tailNode = edge.getSource();
					DotNode headNode = edge.getTarget();
					Set<DotNode> tailNodes = new HashSet<DotNode>();
					tailNodes.add(tailNode);
					Set<DotNode> headNodes = new HashSet<DotNode>();
					headNodes.add(headNode);
					boolean changed = true;
					while (changed) {
						changed = false;
						for (DotEdge anotherEdge : dotGraph.getEdges()) {
							if (isEqual(edge, anotherEdge)) {
								if (tailNodes.contains(anotherEdge.getSource())) {
									changed = changed || headNodes.add(anotherEdge.getTarget());
								}
								if (headNodes.contains(anotherEdge.getTarget())) {
									changed = changed || tailNodes.add(anotherEdge.getSource());
								}
							}
						}
					}

					/*
					 * Get a biggest maximal clique in the cluster.
					 */
					Set<DotEdge> edges = getMaximalClique(dotGraph, tailNodes, headNodes, edge.getOption("arrowtail"),
							edge.getOption("arrowhead"), edge.getLabel(), edge, new HashSet<List<Set<DotNode>>>());

					if (edges != null) {
						/*
						 * A maximal clique was found. Update the sources and targets to this clique.
						 */
						tailNodes.clear();
						headNodes.clear();
						for (DotEdge anotherEdge : edges) {
							tailNodes.add(anotherEdge.getSource());
							headNodes.add(anotherEdge.getTarget());
						}
						/*
						 * Add a connector node to the graph.
						 */
						DotNode connector = dotGraph.addNode("");
						connector.setOption("shape", "point");
						/*
						 * Add edges from and to the new connector node.
						 */
						for (DotNode node : tailNodes) {
							DotEdge anotherEdge = dotGraph.addEdge(node, connector);
							anotherEdge.setOption("dir", "both");
							anotherEdge.setOption("arrowtail", edge.getOption("arrowtail"));
							anotherEdge.setOption("arrowhead", "none");
							if (edge.getOption("taillabel") != null) {
								anotherEdge.setOption("taillabel", edge.getOption("taillabel"));
							}
							if (edge.getLabel() != null) {
								String[] labels2 = edge.getLabel().split("&rarr;");
								if (labels2.length == 2) {
									System.out.println("[LogSkeleton] set label1 " + labels2[0]);
									anotherEdge.setLabel(labels2[0]);
								} else {
									anotherEdge.setLabel(edge.getLabel());
								}
							}
							if (edge.getOption("color") != null) {
								String[] colors2 = edge.getOption("color").split("[;:]");
								if (colors2.length == 4) {
									System.out.println("[LogSkeleton] set color1 " + colors2[0]);
									anotherEdge.setOption("color", colors2[0]);
								} else {
									anotherEdge.setOption("color", edge.getOption("color"));
								}
							}
							candidateEdges.add(anotherEdge);
						}
						for (DotNode node : headNodes) {
							DotEdge anotherEdge = dotGraph.addEdge(connector, node);
							anotherEdge.setOption("dir", "both");
							anotherEdge.setOption("arrowtail", "none");
							anotherEdge.setOption("arrowhead", edge.getOption("arrowhead"));
							if (edge.getOption("headlabel") != null) {
								anotherEdge.setOption("headlabel", edge.getOption("headlabel"));
							}
							if (edge.getLabel() != null) {
								String[] labels2 = edge.getLabel().split("&rarr;");
								if (labels2.length == 2) {
									System.out.println("[LogSkeleton] set label2 " + labels2[1]);
									anotherEdge.setLabel(labels2[1]);
								} else {
									anotherEdge.setLabel(edge.getLabel());
								}
							}
							if (edge.getOption("color") != null) {
								String[] colors2 = edge.getOption("color").split("[;:]");
								if (colors2.length == 4) {
									System.out.println("[LogSkeleton] set color2 " + colors2[2]);
									anotherEdge.setOption("color", colors2[2]);
								} else {
									anotherEdge.setOption("color", edge.getOption("color"));
								}
							}
							candidateEdges.add(anotherEdge);
						}
						/*
						 * Remove the old edges, they have now been replaced with the newly added
						 * connector node and edges.
						 */
						for (DotEdge anotherArc : edges) {
							dotGraph.removeEdge(anotherArc);
						}
						candidateEdges.removeAll(edges);
						/*
						 * Sort the edges again, as some have been added.
						 */
						Collections.sort(candidateEdges, new Comparator<DotEdge>() {

							public int compare(DotEdge o1, DotEdge o2) {
								int c = o1.getSource().getLabel().compareTo(o2.getSource().getLabel());
								if (c == 0) {
									c = o1.getTarget().getLabel().compareTo(o2.getTarget().getLabel());
								}
								return c;
							}

						});
					} else {
						/*
						 * No maximal clique was found, leave the edge as-is.
						 */
						candidateEdges.remove(edge);
					}
				} else {
					/*
					 * Not an always-edge, leave the edge as-is.
					 */
					candidateEdges.remove(edge);
				}
			}
		}
	}

	private void addLegend(LogSkeletonGraph graph, BrowserConfiguration configuration) {

		/*
		 * Add a legend to the dot visualization.
		 */
		dotGraph.setOption("labelloc", "b");
		dotGraph.setOption("nodesep", "0.5");
		if (!configuration.getFontname().isEmpty()) {
			System.out.println("[LogSkeleton] fontname = " + configuration.getFontname());
			dotGraph.setOption("fontname", configuration.getFontname());
		}
		if (!configuration.getFontnameRepresentation().isEmpty()) {
			System.out.println("[LogSkeleton] fontnames = " + configuration.getFontnameRepresentation());
			dotGraph.setOption("fontnames", configuration.getFontnameRepresentation());
		}
		List<String> selectedActivities = new ArrayList<String>(configuration.getActivities());
		Collections.sort(selectedActivities);
		String label = "<table bgcolor=\"gold\" cellborder=\"0\" cellpadding=\"0\" columns=\"3\" style=\"rounded\">";
		label += encodeHeader("Skeleton configuration");
		for (LogSkeletonLegendLine line : graph.getLegendLines()) {
			label += encodeRow(line.getLabel(), line.getValue());
		}
		label += "</table>";
		dotGraph.setOption("fontsize", "8.0");
		dotGraph.setOption("label", "<" + label + ">");
	}

	/*
	 * Encodes the legend header (title).
	 */
	private String encodeHeader(String title) {
		return "<tr><td colspan=\"3\"><b>" + encodeHTML(title) + "</b></td></tr><hr/>";
	}

	/*
	 * Encodes a row in the legend.
	 */
	private String encodeRow(String label, String value) {
		return encodeRow(label, value, 0);
	}

	/*
	 * Encodes a row in the legend.
	 */
	private String encodeRow(String label, String value, int padding) {
		return "<tr><td align=\"right\"><i>" + label + "</i></td><td> : </td><td align=\"left\">" + encodeHTML(value)
				+ "</td></tr>";
	}

	/*
	 * Encodes a string as HTML.
	 */
	private String encodeHTML(String s) {
		String s2 = s;
		if (s.length() > 2 && s.startsWith("[") && s.endsWith("]")) {
			s2 = s.substring(1, s.length() - 1);
		}
		return s2.replaceAll("&", "&amp;").replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
	}

	/*
	 * Returns whether the provided edges have the same head and tail and the same
	 * labels.
	 */
	private boolean isEqual(DotEdge e1, DotEdge e2) {
		if (!isEqual(e1.getOption("arrowtail"), e2.getOption("arrowtail"))) {
			return false;
		}
		if (!isEqual(e1.getOption("arrowhead"), e2.getOption("arrowhead"))) {
			return false;
		}
		if (!isEqual(e1.getOption("headlabel"), e2.getOption("headlabel"))) {
			return false;
		}
		if (!isEqual(e1.getOption("taillabel"), e2.getOption("taillabel"))) {
			return false;
		}
		if (!isEqual(e1.getLabel(), e2.getLabel())) {
			return false;
		}
		return true;
	}

	/*
	 * Returns whether two Strings (possibly null) are equal.
	 */
	private boolean isEqual(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		}
		return s1.equals(s2);
	}

	/*
	 * Returns a maximal clique of similar edges.
	 */
	private Set<DotEdge> getMaximalClique(Dot graph, Set<DotNode> sourceNodes, Set<DotNode> targetNodes,
			String arrowtail, String arrowhead, String label, DotEdge baseEdge, Set<List<Set<DotNode>>> checkedNodes) {
		/*
		 * Make sure a clique is not too small.
		 */
		if (sourceNodes.size() < 2) {
			/*
			 * A single source. Do not look for a maximal clique.
			 */
			return null;
		}
		if (targetNodes.size() < 2) {
			/*
			 * A single target. Do not look for a maximal clique.
			 */
			return null;
		}
		/*
		 * Keep track of which combinations of sources and targets have already been
		 * checked. This prevents checking the same combinations many times over.
		 */
		List<Set<DotNode>> checked = new ArrayList<Set<DotNode>>();
		checked.add(new HashSet<DotNode>(sourceNodes));
		checked.add(new HashSet<DotNode>(targetNodes));
		checkedNodes.add(checked);
		/*
		 * Collect all matching arcs that go from some source to some target.
		 */
		Set<DotEdge> edges = new HashSet<DotEdge>();
		for (DotEdge edge : graph.getEdges()) {
			if (isEqual(edge, baseEdge)) {
				if (sourceNodes.contains(edge.getSource()) && targetNodes.contains(edge.getTarget())) {
					edges.add(edge);
				}
			}
		}
		/*
		 * Check whether a maximal clique.
		 */
		if (edges.size() == sourceNodes.size() * targetNodes.size()) {
			/*
			 * Yes.
			 */
			return edges;
		}
		/*
		 * No, look for maximal cliques that have one node (source or target) less.
		 */
		Set<DotEdge> bestEdges = null; // Best solution so far.
		if (sourceNodes.size() > targetNodes.size()) {
			/*
			 * More sources than targets. Removing a source yields a possible bigger clique
			 * than removing a target. So, first try to remove a source, and only then try
			 * to remove a target.
			 */
			if (sourceNodes.size() > 2) {
				/*
				 * Try to find a maximal clique with one source removed. Sort the source nodes
				 * first to get a (more) deterministic result.
				 */
				List<DotNode> sortedSourceNodes = new ArrayList<DotNode>(sourceNodes);
				Collections.sort(sortedSourceNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode srcNode : sortedSourceNodes) {
					if (bestEdges == null || (sourceNodes.size() - 1) * targetNodes.size() > bestEdges.size()) {
						/*
						 * May result in a bigger clique than the best found so far. First, remove the
						 * node from the sources.
						 */
						Set<DotNode> nodes = new HashSet<DotNode>(sourceNodes);
						nodes.remove(srcNode);
						/*
						 * Check whether this combination of sources and targets was checked before.
						 */
						checked = new ArrayList<Set<DotNode>>();
						checked.add(nodes);
						checked.add(targetNodes);
						if (!checkedNodes.contains(checked)) {
							/*
							 * No, it was not. Check now.
							 */
							edges = getMaximalClique(graph, nodes, targetNodes, arrowtail, arrowhead, label, baseEdge,
									checkedNodes);
							if (bestEdges == null || (edges != null && bestEdges.size() < edges.size())) {
								/*
								 * Found a bigger maximal clique than the best found so far. Update.
								 */
								bestEdges = edges;
							}
						}
					}
				}
			}
			if (targetNodes.size() > 2) {
				List<DotNode> sortedTargetNodes = new ArrayList<DotNode>(targetNodes);
				Collections.sort(sortedTargetNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode tgtNode : sortedTargetNodes) {
					if (bestEdges == null || sourceNodes.size() * (targetNodes.size() - 1) > bestEdges.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(targetNodes);
						nodes.remove(tgtNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(sourceNodes);
						checked.add(nodes);
						if (!checkedNodes.contains(checked)) {
							edges = getMaximalClique(graph, sourceNodes, nodes, arrowtail, arrowhead, label, baseEdge,
									checkedNodes);
							if (bestEdges == null || (edges != null && bestEdges.size() < edges.size())) {
								bestEdges = edges;
							}
						}
					}
				}
			}
		} else {
			/*
			 * The other way around.
			 */
			if (targetNodes.size() > 2) {
				List<DotNode> sortedTargetNodes = new ArrayList<DotNode>(targetNodes);
				Collections.sort(sortedTargetNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode tgtNode : sortedTargetNodes) {
					if (bestEdges == null || sourceNodes.size() * (targetNodes.size() - 1) > bestEdges.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(targetNodes);
						nodes.remove(tgtNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(sourceNodes);
						checked.add(nodes);
						if (!checkedNodes.contains(checked)) {
							edges = getMaximalClique(graph, sourceNodes, nodes, arrowtail, arrowhead, label, baseEdge,
									checkedNodes);
							if (bestEdges == null || (edges != null && bestEdges.size() < edges.size())) {
								bestEdges = edges;
							}
						}
					}
				}
			}
			if (sourceNodes.size() > 2) {
				List<DotNode> sortedSourceNodes = new ArrayList<DotNode>(sourceNodes);
				Collections.sort(sortedSourceNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode srcNode : sortedSourceNodes) {
					if (bestEdges == null || (sourceNodes.size() - 1) * targetNodes.size() > bestEdges.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(sourceNodes);
						nodes.remove(srcNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(nodes);
						checked.add(targetNodes);
						if (!checkedNodes.contains(checked)) {
							edges = getMaximalClique(graph, nodes, targetNodes, arrowtail, arrowhead, label, baseEdge,
									checkedNodes);
							if (bestEdges == null || (edges != null && bestEdges.size() < edges.size())) {
								bestEdges = edges;
							}
						}
					}
				}
			}
		}
		/*
		 * Return the biggest maximal clique found. Equals null if none found.
		 */
		return bestEdges;
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
