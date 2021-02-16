package org.processmining.logskeleton.algorithms;

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

	public Dot apply(LogSkeletonGraph graph, BrowserConfiguration configuration) {
		Map<LogSkeletonNode, DotNode> map = new HashMap<LogSkeletonNode, DotNode>();
		Dot dotGraph = new Dot();

		for (LogSkeletonNode node : graph.getNodes()) {
			DotNode dotNode = dotGraph.addNode("<<table align=\"center\" bgcolor=\"" + node.getBackgroundColor()
					+ "\" border=\"" + (node.hasBorder() ? "1" : "0")
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

		for (LogSkeletonEdge edge : graph.getEdges()) {
			DotEdge dotEdge = dotGraph.addEdge(map.get(edge.getSource()), map.get(edge.getTarget()));
			dotEdge.setOption("dir", "both");
			String headDecorator = "";
			String tailDecorator = "";
			if (edge.getHead() != null) {
				switch (edge.getHead()) {
					case ALWAYS : {
						headDecorator = "normal";
						break;
					}
					case NEVER : {
						if (configuration.isUseInvertedArrows()) {
							headDecorator = "noneinvtee";
						} else {
							headDecorator = "onormal";
						}
						break;
					}
					case EXCLUSIVE : {
						headDecorator = "nonetee";
						break;
					}
				}
			}
			if (edge.getTail() != null) {
				switch (edge.getTail()) {
					case ALWAYS : {
						tailDecorator = "noneinv";
						break;
					}
					case NEVER : {
						if (configuration.isUseInvertedArrows()) {
							tailDecorator = "teenormal";
						} else {
							tailDecorator = "noneoinv";
						}
						break;
					}
					case EXCLUSIVE : {
						tailDecorator = "nonetee";
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

			if (configuration.isUseEdgeColors() && (edge.getHeadColor() != null || edge.getTailColor() != null)) {
				/*
				 * Color the edges.
				 */
				String color = (edge.getTailColor() == null ? graph.getDefaultEdgeColor() : edge.getTailColor())
						+ ";0.5:" + (edge.getHeadColor() == null ? graph.getDefaultEdgeColor() : edge.getHeadColor())
						+ ";0.5";
				dotEdge.setOption("color", color);
			}

			if (configuration.isUseHeadTailLabels()) {
				/*
				 * Show labels seperatley at head/tail
				 */
				if (edge.getHeadLabel() != null) {
					dotEdge.setOption("headlabel", edge.getHeadLabel());
				}
				if (edge.getTailLabel() != null) {
					dotEdge.setOption("taillabel", edge.getTailLabel());
				}
			} else if (edge.getHeadLabel() != null || edge.getTailLabel() != null) {
				/*
				 * Show labels combined at middle of arc
				 */
				String label = "";
				if (edge.getTailLabel() != null) {
					label += edge.getTailLabel();
				}
				label += "&rarr;";
				if (edge.getHeadLabel() != null) {
					label += edge.getHeadLabel();
				}
				dotEdge.setLabel(label);
			}
		}

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
					DotNode sourceNode = edge.getSource();
					DotNode targetNode = edge.getTarget();
					Set<DotNode> sourceNodes = new HashSet<DotNode>();
					sourceNodes.add(sourceNode);
					Set<DotNode> targetNodes = new HashSet<DotNode>();
					targetNodes.add(targetNode);
					boolean changed = true;
					while (changed) {
						changed = false;
						for (DotEdge anotherEdge : dotGraph.getEdges()) {
							if (isEqual(edge, anotherEdge)) {
								if (sourceNodes.contains(anotherEdge.getSource())) {
									changed = changed || targetNodes.add(anotherEdge.getTarget());
								}
								if (targetNodes.contains(anotherEdge.getTarget())) {
									changed = changed || sourceNodes.add(anotherEdge.getSource());
								}
							}
						}
					}

					/*
					 * Get a biggest maximal clique in the cluster.
					 */
					Set<DotEdge> edges = getMaximalClique(dotGraph, sourceNodes, targetNodes, edge.getOption("arrowtail"),
							edge.getOption("arrowhead"), edge.getLabel(), edge, new HashSet<List<Set<DotNode>>>());

					if (edges != null) {
						/*
						 * A maximal clique was found. Update the sources and targets to this clique.
						 */
						sourceNodes.clear();
						targetNodes.clear();
						for (DotEdge anotherEdge : edges) {
							sourceNodes.add(anotherEdge.getSource());
							targetNodes.add(anotherEdge.getTarget());
						}
						/*
						 * Add a connector node to the graph.
						 */
						DotNode connector = dotGraph.addNode("");
						connector.setOption("shape", "point");
						/*
						 * Add edges from and to the new connector node.
						 */
						for (DotNode node : sourceNodes) {
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
						for (DotNode node : targetNodes) {
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

		return dotGraph;
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
}
