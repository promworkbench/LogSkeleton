package org.processmining.logskeleton.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.HTMLToString;
import org.processmining.logskeleton.parameters.LogSkeletonBrowser;
import org.processmining.logskeleton.parameters.LogSkeletonBrowserParameters;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class LogSkeleton implements HTMLToString {

	private LogSkeletonCount countModel;
	private Collection<Collection<String>> sameCounts;
	private Map<String, Set<String>> allPresets;
	private Map<String, Set<String>> allPostsets;
	private Map<String, Set<String>> anyPresets;
	private Map<String, Set<String>> anyPostsets;

	private Set<String> required;
	private Set<String> forbidden;
	private List<List<String>> splitters;
	private String label;

	//	private Map<List<String>, List<Integer>> distances;

	public LogSkeleton() {
		this(new LogSkeletonCount());
	}

	public LogSkeleton(LogSkeletonCount countModel) {
		this.countModel = countModel;
		sameCounts = new HashSet<Collection<String>>();
		allPresets = new HashMap<String, Set<String>>();
		allPostsets = new HashMap<String, Set<String>>();
		anyPresets = new HashMap<String, Set<String>>();
		anyPostsets = new HashMap<String, Set<String>>();
		//		distances = new HashMap<List<String>, List<Integer>>();
		required = new HashSet<String>();
		forbidden = new HashSet<String>();
		splitters = new ArrayList<List<String>>();
		label = null;
	}

	public void addSameCount(Collection<String> activities) {
		List<String> orderedActivities = new ArrayList<String>(activities);
		Collections.sort(orderedActivities);
		sameCounts.add(orderedActivities);
	}

	public Collection<String> getSameCounts(String activity) {
		for (Collection<String> sameCount : sameCounts) {
			if (sameCount.contains(activity)) {
				return sameCount;
			}
		}
		return null;
	}

	public void addPrePost(String activity, List<String> pre, List<String> post) {
		Set<String> preset = new HashSet<String>(pre);
		Set<String> postset = new HashSet<String>(post);
		if (allPresets.containsKey(activity)) {
			preset.retainAll(allPresets.get(activity));
		}
		allPresets.put(activity, preset);
		if (allPostsets.containsKey(activity)) {
			postset.retainAll(allPostsets.get(activity));
		}
		allPostsets.put(activity, postset);
		preset = new HashSet<String>(pre);
		postset = new HashSet<String>(post);
		if (anyPresets.containsKey(activity)) {
			preset.addAll(anyPresets.get(activity));
		}
		anyPresets.put(activity, preset);
		if (anyPostsets.containsKey(activity)) {
			postset.addAll(anyPostsets.get(activity));
		}
		anyPostsets.put(activity, postset);
		//		int distance = 1;
		//		for (String postActivity : post) {
		//			List<String> pair = new ArrayList<String>();
		//			pair.add(activity);
		//			pair.add(postActivity);
		//			if (!distances.containsKey(pair)) {
		//				distances.put(pair, new ArrayList<Integer>());
		//			}
		//			distances.get(pair).add(distance);
		//			distance++;
		//		}
	}

	public void cleanPrePost() {
		for (String activity : countModel.getActivities()) {
			cleanPrePost(activity, allPresets);
			cleanPrePost(activity, allPostsets);
			//			cleanPrePost(activity, anyPresets);
			//			cleanPrePost(activity, anyPostsets);
		}
	}

	private void cleanPrePost(String activity, Map<String, Set<String>> map) {
		Set<String> mappedActivities = map.get(activity);
		Set<String> mappedMappedActivities = new HashSet<String>();
		for (String mappedActivity : mappedActivities) {
			for (String mappedMappedActivity : map.get(mappedActivity)) {
				if (!map.get(mappedMappedActivity).contains(mappedActivity)) {
					mappedMappedActivities.add(mappedMappedActivity);
				}
			}
		}
		mappedActivities.removeAll(mappedMappedActivities);
		map.put(activity, mappedActivities);
	}

	private boolean checkSameCounts(LogSkeletonCount model, Set<String> messages, String caseId) {
		boolean ok = true;
		for (Collection<String> sameCount : sameCounts) {
			Set<Integer> counts = new HashSet<Integer>();
			for (String activity : sameCount) {
				counts.add(model.get(activity));
			}
			if (counts.size() != 1) {
				messages.add("[LogSkeleton] Case " + caseId + ": Always Together fails for " + sameCount);
				ok = false;
			}
		}
		return ok;
	}

	private boolean checkTransitionCounts(LogSkeletonCount model, Set<String> messages, String caseId) {
		return countModel.checkTransitionCounts(model, messages, caseId);
	}

	//	private boolean checkDistance(int distance, List<Integer> distances) {
	//		if (distance > 0) {
	//			return true;
	//		}
	//		if (distance > 2 || distances.size() < 10) {
	//			return true;
	//		}
	//		int min = Integer.MAX_VALUE;
	//		int max = 0;
	//		for (Integer value : distances) {
	//			min = Math.min(min, value);
	//			max = Math.max(max, value);
	//		}
	//		if (min > distance || distance > max) {
	//			System.out.println("[PDC2017ConstraintModel] " + distance + " " + distances);
	//		}
	//		return min <= distance && distance <= max;
	//	}

	private boolean checkCausalDependencies(XTrace trace, Set<String> messages) {
		String caseId = XConceptExtension.instance().extractName(trace);
		List<String> postset = new ArrayList<String>();
		postset.add(LogSkeletonCount.STARTEVENT);
		for (XEvent event : trace) {
			postset.add(XConceptExtension.instance().extractName(event));
		}
		postset.add(LogSkeletonCount.ENDEVENT);
		List<String> preset = new ArrayList<String>();
		String prevActivity = null;
		while (!postset.isEmpty()) {
			if (prevActivity != null) {
				preset.add(prevActivity);
				//				int distance = 1;
				//				for (String postActivity : postset) {
				//					List<String> pair = new ArrayList<String>();
				//					pair.add(prevActivity);
				//					pair.add(postActivity);
				//					if (distances.containsKey(pair) && !checkDistance(distance, distances.get(pair))) {
				//						//						return false;
				//					}
				//					distance++;
				//				}
			}
			String activity = postset.remove(0);
			if (allPresets.containsKey(activity) && !preset.containsAll(allPresets.get(activity))) {
				Set<String> missing = new HashSet<String>(allPresets.get(activity));
				missing.removeAll(preset);
				messages.add("[LogSkeleton] Case " + caseId + ": Always Before fails for " + activity
						+ ", missing are " + missing);
				return false;
			}
			if (allPostsets.containsKey(activity) && !postset.containsAll(allPostsets.get(activity))) {
				Set<String> missing = new HashSet<String>(allPostsets.get(activity));
				missing.removeAll(postset);
				messages.add("[LogSkeleton] Case " + caseId + ": Always After fails for " + activity + ", missing are "
						+ missing);
				return false;
			}
			//			if (!anyPresets.get(activity).containsAll(preset)) {
			//				System.out.println("[LogSkeleton] Sometimes Before fails on " + prevActivity + " and " + activity);
			//				return false;
			//			}
			//			if (!anyPostsets.get(activity).containsAll(postset)) {
			//				System.out.println("[LogSkeleton] Sometimes After fails on " + prevActivity + " and " + activity);
			//				return false;
			//			}
			prevActivity = activity;
		}
		return true;
	}

	public boolean check(XTrace trace, LogSkeletonCount model, Set<String> messages, boolean[] checks) {
		boolean ok = true;
		if (checks[0]) {
			ok = ok && checkSameCounts(model, messages, XConceptExtension.instance().extractName(trace));
			if (!ok) {
				return false;
			}
		}
		if (checks[1]) {
			ok = ok && checkCausalDependencies(trace, messages);
			if (!ok) {
				return false;
			}
		}
		if (checks[2]) {
			ok = ok && checkTransitionCounts(model, messages, XConceptExtension.instance().extractName(trace));
			if (!ok) {
				return false;
			}
		}
		return ok;
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buf = new StringBuffer();
		List<String> sorted;
		if (includeHTMLTags) {
			buf.append("<html>");
		}
		//		buf.append("<h1>Count similarities</h1><table>");
		//		buf.append("<tr><th>Actrivities</th><th>Count</th></tr>");
		//		for (Collection<String> activity : sameCounts) {
		//			// Activities
		//			sorted = new ArrayList<String>(activity);
		//			Collections.sort(sorted);
		//			buf.append("<tr><td>" + sorted + "</td>");
		//			// Count
		//			buf.append("<td>" + countModel.get(activity.iterator().next()) + "</td></tr>");
		//		}
		//		buf.append("</table>");
		//		buf.append("<h1>Count relations</h1><table>");
		//		buf.append("<tr><th>From activities</th><th>Relation</th><th>To activities</th></tr>");
		//		for (List<Collection<String>> activities : placeCounts.keySet()) {
		//			// From activities
		//			sorted = new ArrayList<String>(activities.get(0));
		//			Collections.sort(sorted);
		//			buf.append("<tr><td>" + sorted + "</td>");
		//			// Relation
		//			buf.append("<td>" + placeCounts.get(activities) + "</td>");
		//			// To activities
		//			sorted = new ArrayList<String>(activities.get(1));
		//			Collections.sort(sorted);
		//			buf.append("<td>" + sorted + "</td></tr>");
		//		}
		//		buf.append("</table>");
		buf.append("<h1>Causal relations</h1><table>");
		buf.append("<tr><th>Activity</th><th>Sibling activities</th><th>Count</th><th>Always before</th><th>Always after</th><th>Never together</th><th>Never before</th><th>Never after</th></tr>");
		for (String activity : countModel.getActivities()) {
			// Activity
			buf.append("<tr><td>" + activity + "</td>");
			// Sibling activities and count
			for (Collection<String> siblings : sameCounts) {
				if (siblings.contains(activity)) {
					// Activities
					sorted = new ArrayList<String>(siblings);
					Collections.sort(sorted);
					sorted.remove(activity);
					buf.append("<td>" + sorted + "</td>");
					// Count
					buf.append("<td>" + countModel.get(activity) + "</td>");
				}
			}
			// Always before
			sorted = new ArrayList<String>(allPresets.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Always after
			sorted = new ArrayList<String>(allPostsets.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Never together
			sorted = new ArrayList<String>(countModel.getActivities());
			sorted.removeAll(anyPresets.get(activity));
			sorted.removeAll(anyPostsets.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Never before
			sorted = new ArrayList<String>(countModel.getActivities());
			sorted.removeAll(anyPresets.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Never after
			sorted = new ArrayList<String>(countModel.getActivities());
			sorted.removeAll(anyPostsets.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td></tr>");
		}
		buf.append("</table>");
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

	public Dot visualize(LogSkeletonBrowserParameters parameters) {
		Map<String, DotNode> map = new HashMap<String, DotNode>();
		Dot graph = new Dot();
		//		graph.setOption("concentrate", "true");
		//		graph.setKeepOrderingOfChildren(true);
		// Set312 color scheme, with white as last resort.
		String[] set312Colors = new String[] { "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462",
				"#b3de69", "#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f" };
		//		String[] colors = new String[] { "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462", "#b3de69",
		//				"#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f", "#8dd3c7:#ffffb3", "#bebada:#fb8072",
		//				"#80b1d3:#fdb462", "#b3de69:#fccde5", "#d9d9d9:#bc80bd", "#ccebc5:#ffed6f", "#ffffb3:#bebada",
		//				"#fb8072:#80b1d3", "#fdb462:#b3de69", "#fccde5:#d9d9d9", "#bc80bd:#ccebc5", "#ffed6f:#8dd3c7", "white" };
		String[] colors = new String[100];
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

		int colorIndex = 0;
		//		System.out.println("[PDC2017ConstrainModel] Activities = " + parameters.getActivities());
		//		System.out.println("[PDC2017ConstrainModel] Visualizers = " + parameters.getVisualizers());
		Map<String, String> colorMap = new HashMap<String, String>();
		for (String activity : parameters.getActivities()) {
			String colorActivity = getSameCounts(activity).iterator().next();
			String activityColor = colorMap.get(colorActivity);
			if (activityColor == null) {
				activityColor = colors[colorIndex];
				colorMap.put(colorActivity, activityColor);
				if (colorIndex < colors.length - 1) {
					colorIndex++;
				}
			}
			String interval = "" + countModel.getMin(activity);
			if (countModel.getMax(activity) > countModel.getMin(activity)) {
				interval += ".." + countModel.getMax(activity);
			}
			DotNode node = graph
					.addNode("<<table align=\"center\" bgcolor=\""
							+ activityColor
							+ "\" border=\"1\" cellborder=\"0\" cellpadding=\"2\" columns=\"*\" style=\"rounded\"><tr><td colspan=\"3\"><font point-size=\"24\"><b>"
							+ encodeHTML(activity) + "</b></font></td></tr><hr/><tr><td>" + colorActivity + "</td><td>"
							+ countModel.get(activity) + "</td>" + "<td>" + interval + "</td>" + "</tr></table>>");
			node.setOption("shape", "none");
			//			DotNode node = graph.addNode(activity + "\n" + countModel.get(activity));
			//			node.setLabel("<" + encodeHTML(activity) + ">");
			map.put(activity, node);
		}
		//		for (String activity : parameters.getActivities()) {
		//			String colorActivity = getSameCounts(activity).iterator().next();
		//			String activityColor = colorMap.get(colorActivity);
		//			if (activityColor == null) {
		//				activityColor = colors[colorIndex];
		//				colorMap.put(colorActivity, activityColor);
		//				if (colorIndex < colors.length - 1) {
		//					colorIndex++;
		//				}
		//			}
		//			DotNode node = graph
		//					.addNode("<<table align=\"center\" bgcolor=\""
		//							+ activityColor
		//							+ "\" border=\"1\" cellborder=\"0\" cellpadding=\"2\" columns=\"*\" style=\"rounded\"><tr><td colspan=\"2\"><font point-size=\"24\"><b>"
		//							+ encodeHTML(activity) + "</b></font></td></tr><hr/><tr><td>" + colorActivity + "</td><td>"
		//							+ countModel.get(activity) + "</td></tr></table>>");
		//			node.setOption("shape", "none");
		//			//			DotNode node = graph.addNode(activity + "\n" + countModel.get(activity));
		//			//			node.setLabel("<" + encodeHTML(activity) + ">");
		//			map.put(activity, node);
		//		}

		for (String fromActivity : parameters.getActivities()) {
			if (parameters.getActivities().contains(fromActivity)) {
				for (String toActivity : parameters.getActivities()) {
					if (parameters.getActivities().contains(toActivity)) {
						String tailDecorator = null;
						String headDecorator = null;
						String tailLabel = null;
						String headLabel = null;
						String tailArrow = null;
						String headArrow = null;
						boolean constraint = true;
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.ALWAYSAFTER)) {
							if (tailDecorator == null && allPostsets.get(fromActivity).contains(toActivity)) {
								tailDecorator = "obox";
								headArrow = "normal";
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.ALWAYSBEFORE)) {
							if (headDecorator == null && allPresets.get(toActivity).contains(fromActivity)) {
								headDecorator = "obox";
								headArrow = "normal";
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.OFTENNEXT)) {
							if (tailDecorator == null && countModel.get(toActivity, fromActivity) == 0
									&& (5 * countModel.get(fromActivity, toActivity) > countModel.get(fromActivity))) {
								tailDecorator = "odot";
								headArrow = "normal";
								headLabel = "" + countModel.get(fromActivity, toActivity);
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.OFTENPREVIOUS)) {
							if (headDecorator == null && countModel.get(toActivity, fromActivity) == 0
									&& (5 * countModel.get(fromActivity, toActivity) > countModel.get(toActivity))) {
								headDecorator = "odot";
								headArrow = "normal";
								headLabel = "" + countModel.get(fromActivity, toActivity);
							}
						}
//						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERTOGETHERSELF)) {
//							if (fromActivity.equals(toActivity)) {
//								if (headDecorator == null && fromActivity.compareTo(toActivity) >= 0
//										&& !anyPresets.get(fromActivity).contains(toActivity)
//										&& !anyPostsets.get(fromActivity).contains(toActivity)) {
//									headDecorator = "box";
//								}
//								if (tailDecorator == null && fromActivity.compareTo(toActivity) >= 0
//										&& !anyPresets.get(fromActivity).contains(toActivity)
//										&& !anyPostsets.get(fromActivity).contains(toActivity)) {
//									tailDecorator = "box";
//								}
//							}
//						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERTOGETHER)) {
							if (!fromActivity.equals(toActivity)) {
								if (headDecorator == null && fromActivity.compareTo(toActivity) >= 0
										&& !anyPresets.get(fromActivity).contains(toActivity)
										&& !anyPostsets.get(fromActivity).contains(toActivity)) {
									headDecorator = "box";
									constraint = false;
								}
								if (tailDecorator == null && fromActivity.compareTo(toActivity) >= 0
										&& !anyPresets.get(fromActivity).contains(toActivity)
										&& !anyPostsets.get(fromActivity).contains(toActivity)) {
									tailDecorator = "box";
									constraint = false;
								}
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEXTONEWAY)) {
							if (tailDecorator == null && countModel.get(fromActivity, toActivity) > 0
									&& countModel.get(toActivity, fromActivity) == 0) {
								tailDecorator = "odot";
								headLabel = "" + countModel.get(fromActivity, toActivity);
								headArrow = "normal";
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEXTBOTHWAYS)) {
							if (fromActivity.compareTo(toActivity) <= 0) {
								if (tailDecorator == null && countModel.get(fromActivity, toActivity) > 0
										&& countModel.get(toActivity, fromActivity) > 0) {
									tailDecorator = "odot";
									headLabel = "" + countModel.get(fromActivity, toActivity);
									headArrow = "normal";
									constraint = false;
								}
								if (headDecorator == null && countModel.get(fromActivity, toActivity) > 0
										&& countModel.get(toActivity, fromActivity) > 0) {
									headDecorator = "odot";
									tailLabel = "" + countModel.get(toActivity, fromActivity);
									tailArrow = "vee";
									constraint = false;
								}
							}
						}
						if (tailDecorator != null || headDecorator != null || tailArrow != null || headArrow != null) {
							DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
							arc.setOption("dir", "both");
							if (tailDecorator == null) {
								tailDecorator = "";
							}
							if (tailArrow == null) {
								tailArrow = "none";
							}
							if (headDecorator == null) {
								headDecorator = "";
							}
							if (headArrow == null) {
								headArrow = "none";
							}
							arc.setOption("arrowtail", tailDecorator + tailArrow);
							arc.setOption("arrowhead", headDecorator + headArrow);
							//							if (!constraint) {
							//								arc.setOption("constraint", "false");
							//							}
							//							arc.setOption("constraint", "true");
							if (headLabel != null) {
								if (tailLabel == null) {
									arc.setLabel(headLabel);
								} else {
									arc.setLabel(headLabel + "/" + tailLabel);
								}
							}
						}
					}
				}
			}
		}

		if (parameters.isUseHyperArcs()) {
			/*
			 * Sort the arcs to get a (more) deterministic result.
			 */
			List<DotEdge> candidateArcs = new ArrayList<DotEdge>(graph.getEdges());
			Collections.sort(candidateArcs, new Comparator<DotEdge>() {

				public int compare(DotEdge o1, DotEdge o2) {
					int c = o1.getSource().getLabel().compareTo(o2.getSource().getLabel());
					if (c == 0) {
						c = o1.getTarget().getLabel().compareTo(o2.getTarget().getLabel());
					}
					return c;
				}

			});

			/*
			 * Iterate over all arcs in the (current!) graph.
			 * 
			 * Note that the graph may change in the process.
			 */
			while (!candidateArcs.isEmpty()) {
				/*
				 * Get the next arc.
				 */
				DotEdge arc = candidateArcs.iterator().next();
				/*
				 * For now, only do this for always-arcs.
				 */
				if (arc.getOption("arrowtail").contains("obox") || arc.getOption("arrowhead").contains("obox")) {
					/*
					 * Get the cluster for this arc.
					 */
					DotNode sourceNode = arc.getSource();
					DotNode targetNode = arc.getTarget();
					Set<DotNode> sourceNodes = new HashSet<DotNode>();
					sourceNodes.add(sourceNode);
					Set<DotNode> targetNodes = new HashSet<DotNode>();
					targetNodes.add(targetNode);
					boolean changed = true;
					while (changed) {
						changed = false;
						for (DotEdge anotherArc : graph.getEdges()) {
							if (arc != anotherArc
									&& arc.getOption("arrowtail").equals(anotherArc.getOption("arrowtail"))
									&& arc.getOption("arrowhead").equals(anotherArc.getOption("arrowhead"))
									&& (arc.getLabel() == null ? anotherArc.getLabel() == null : arc.getLabel().equals(
											anotherArc.getLabel()))) {
								if (sourceNodes.contains(anotherArc.getSource())) {
									changed = changed || targetNodes.add(anotherArc.getTarget());
								}
								if (targetNodes.contains(anotherArc.getTarget())) {
									changed = changed || sourceNodes.add(anotherArc.getSource());
								}
							}
						}
					}

					/*
					 * Get a biggest maximal clique in the cluster.
					 */
					Set<DotEdge> arcs = getMaximalClique(graph, sourceNodes, targetNodes, arc.getOption("arrowtail"),
							arc.getOption("arrowhead"), arc.getLabel(), new HashSet<List<Set<DotNode>>>());

					if (arcs != null) {
						/*
						 * A maximal clique was found. Update the sources and
						 * targets to this clique.
						 */
						sourceNodes.clear();
						targetNodes.clear();
						for (DotEdge a : arcs) {
							sourceNodes.add(a.getSource());
							targetNodes.add(a.getTarget());
						}
						//						System.out.println("[LogSkeleton] " + sourceNodes + " -> " + targetNodes);
						/*
						 * Add a connector node to the graph.
						 */
						DotNode connector = graph.addNode("");
						connector.setOption("shape", "point");
						/*
						 * Add arcs from and to the new connector node.
						 */
						for (DotNode node : sourceNodes) {
							DotEdge a = graph.addEdge(node, connector);
							a.setOption("dir", "both");
							a.setOption("arrowtail", arc.getOption("arrowtail"));
							a.setOption("arrowhead", "none");
							candidateArcs.add(a);
						}
						for (DotNode node : targetNodes) {
							DotEdge a = graph.addEdge(connector, node);
							a.setOption("dir", "both");
							a.setOption("arrowtail", "none");
							a.setOption("arrowhead", arc.getOption("arrowhead"));
							candidateArcs.add(a);
						}
						/*
						 * Remove the old arcs, they have now been replaced with
						 * the newly added connector node and arcs.
						 */
						for (DotEdge anotherArc : arcs) {
							graph.removeEdge(anotherArc);
						}
						candidateArcs.removeAll(arcs);
						/*
						 * Sort the arcs again, as some have been added.
						 */
						Collections.sort(candidateArcs, new Comparator<DotEdge>() {

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
						 * No maximal clique was found, leave the arc as-is.
						 */
						candidateArcs.remove(arc);
					}
				} else {
					/*
					 * Not an always-arc, leave the arc as-is.
					 */
					candidateArcs.remove(arc);
				}
			}
		}

		graph.setOption("labelloc", "b");
		//		String label = "Event Log: " + (this.label == null ? "<not specified>" : this.label) + "\\l";
		//		if (!required.isEmpty()) {
		//			label += "Required Activities Filters: " + required + "\\l";
		//		}
		//		if (!forbidden.isEmpty()) {
		//			label += "Forbidden Activities Filters: " + forbidden + "\\l";
		//		}
		//		if (!splitters.isEmpty()) {
		//			label += "Activity Splitters: " + splitters + "\\l";
		//		}
		List<String> activities = new ArrayList<String>(parameters.getActivities());
		Collections.sort(activities);
		//		label += "Show Activities: " + activities + "\\l";
		//		label += "Show Constraints: " + parameters.getVisualizers() + "\\l";
		String label = "<table bgcolor=\"gold\" cellborder=\"0\" cellpadding=\"0\" columns=\"3\" style=\"rounded\">";
		label += encodeHeader("Skeleton Configuration");
		label += encodeRow("Event Log", this.label == null ? "<not specified>" : this.label);
		if (!required.isEmpty()) {
			label += encodeRow("Required Activities Filter", required.toString());
		}
		if (!forbidden.isEmpty()) {
			label += encodeRow("Forbidden Activities Filter", forbidden.toString());
		}
		if (!splitters.isEmpty()) {
			label += encodeRow("Activity Splitters", splitters.toString());
		}
		label += encodeRow("View Activities", activities.toString());
		label += encodeRow("View Constraints", parameters.getVisualizers().toString());
		label += "</table>";
		graph.setOption("fontsize", "8.0");
		graph.setOption("label", "<" + label + ">");
		//		graph.setOption("labeljust", "l");
		return graph;
	}

	private Set<DotEdge> getMaximalClique(Dot graph, Set<DotNode> sourceNodes, Set<DotNode> targetNodes,
			String arrowtail, String arrowhead, String label, Set<List<Set<DotNode>>> checkedNodes) {
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
		 * Keep track of which combinations of sources and targets have already
		 * been checked. This prevents checking the same combinations many times
		 * over.
		 */
		List<Set<DotNode>> checked = new ArrayList<Set<DotNode>>();
		checked.add(new HashSet<DotNode>(sourceNodes));
		checked.add(new HashSet<DotNode>(targetNodes));
		checkedNodes.add(checked);
		/*
		 * Collect all matching arcs that go from some source to some target.
		 */
		Set<DotEdge> arcs = new HashSet<DotEdge>();
		for (DotEdge arc : graph.getEdges()) {
			if (arc.getOption("arrowtail").equals(arrowtail) && arc.getOption("arrowhead").equals(arrowhead)
					&& (arc.getLabel() == null ? label == null : arc.getLabel().equals(label))) {
				if (sourceNodes.contains(arc.getSource()) && targetNodes.contains(arc.getTarget())) {
					arcs.add(arc);
				}
			}
		}
		/*
		 * Check whether a maximal clique.
		 */
		if (arcs.size() == sourceNodes.size() * targetNodes.size()) {
			/*
			 * Yes.
			 */
			return arcs;
		}
		/*
		 * No, look for maximal cliques that have one node (source or target)
		 * less.
		 */
		Set<DotEdge> bestArcs = null; // Best solution so far.
		if (sourceNodes.size() > targetNodes.size()) {
			/*
			 * More sources than targets. Removing a source yields a possible
			 * bigger clique than removing a target. So, first try to remove a
			 * source, and only then try to remove a target.
			 */
			if (sourceNodes.size() > 2) {
				/*
				 * Try to find a maximal clique with one source removed. Sort
				 * the source nodes first to get a (more) deterministic result.
				 */
				List<DotNode> sortedSourceNodes = new ArrayList<DotNode>(sourceNodes);
				Collections.sort(sortedSourceNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode srcNode : sortedSourceNodes) {
					if (bestArcs == null || (sourceNodes.size() - 1) * targetNodes.size() > bestArcs.size()) {
						/*
						 * May result in a bigger clique than the best found so
						 * far. First, remove the node from the sources.
						 */
						Set<DotNode> nodes = new HashSet<DotNode>(sourceNodes);
						nodes.remove(srcNode);
						/*
						 * Check whether this combination of sources and targets
						 * was checked before.
						 */
						checked = new ArrayList<Set<DotNode>>();
						checked.add(nodes);
						checked.add(targetNodes);
						if (!checkedNodes.contains(checked)) {
							/*
							 * No, it was not. Check now.
							 */
							arcs = getMaximalClique(graph, nodes, targetNodes, arrowtail, arrowhead, label,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								/*
								 * Found a bigger maximal clique than the best
								 * found so far. Update.
								 */
								bestArcs = arcs;
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
					if (bestArcs == null || sourceNodes.size() * (targetNodes.size() - 1) > bestArcs.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(targetNodes);
						nodes.remove(tgtNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(sourceNodes);
						checked.add(nodes);
						if (!checkedNodes.contains(checked)) {
							arcs = getMaximalClique(graph, sourceNodes, nodes, arrowtail, arrowhead, label,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								bestArcs = arcs;
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
					if (bestArcs == null || sourceNodes.size() * (targetNodes.size() - 1) > bestArcs.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(targetNodes);
						nodes.remove(tgtNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(sourceNodes);
						checked.add(nodes);
						if (!checkedNodes.contains(checked)) {
							arcs = getMaximalClique(graph, sourceNodes, nodes, arrowtail, arrowhead, label,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								bestArcs = arcs;
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
					if (bestArcs == null || (sourceNodes.size() - 1) * targetNodes.size() > bestArcs.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(sourceNodes);
						nodes.remove(srcNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(nodes);
						checked.add(targetNodes);
						if (!checkedNodes.contains(checked)) {
							arcs = getMaximalClique(graph, nodes, targetNodes, arrowtail, arrowhead, label,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								bestArcs = arcs;
							}
						}
					}
				}
			}
		}
		/*
		 * Return the biggest maximal clique found. Equals null if none found.
		 */
		return bestArcs;
	}

	private String encodeHeader(String title) {
		return "<tr><td colspan=\"3\"><b>" + encodeHTML(title) + "</b></td></tr><hr/>";
	}

	private String encodeRow(String label, String value) {
		return encodeRow(label, value, 0);
	}

	private String encodeRow(String label, String value, int padding) {
		return "<tr><td align=\"right\"><i>" + label + "</i></td><td> : </td><td align=\"left\">" + encodeHTML(value)
				+ "</td></tr>";
	}

	private String encodeHTML(String s) {
		String s2 = s;
		if (s.length() > 2 && s.startsWith("[") && s.endsWith("]")) {
			s2 = s.substring(1, s.length() - 1);
		}
		return s2.replaceAll("&", "&amp;").replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
	}

	public Dot createGraph(LogSkeletonBrowser visualizer) {
		LogSkeletonBrowserParameters parameters = new LogSkeletonBrowserParameters();
		parameters.getActivities().addAll(countModel.getActivities());
		parameters.getVisualizers().add(visualizer);
		return visualize(parameters);
	}

	public Dot createGraph(Set<LogSkeletonBrowser> visualizers) {
		LogSkeletonBrowserParameters parameters = new LogSkeletonBrowserParameters();
		parameters.getActivities().addAll(countModel.getActivities());
		parameters.getVisualizers().addAll(visualizers);
		return visualize(parameters);
	}

	public Dot createGraph(LogSkeletonBrowserParameters parameters) {
		parameters.getActivities().addAll(parameters.getActivities());
		parameters.getVisualizers().addAll(parameters.getVisualizers());
		return visualize(parameters);
	}

	public Collection<String> getActivities() {
		return countModel.getActivities();
	}

	public Set<String> getRequired() {
		return required;
	}

	public void setRequired(Set<String> required) {
		this.required = required;
	}

	public Set<String> getForbidden() {
		return forbidden;
	}

	public void setForbidden(Set<String> forbidden) {
		this.forbidden = forbidden;
	}

	public List<List<String>> getSplitters() {
		return splitters;
	}

	public void setSplitters(List<List<String>> splitters) {
		this.splitters = splitters;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void exportToFile(CsvWriter writer) throws IOException {
		writer.write(label);
		writer.endRecord();
		countModel.exportToFile(writer);
		writer.write("always together");
		writer.write("" + sameCounts.size());
		writer.endRecord();
		for (Collection<String> activities : sameCounts) {
			for (String activity : activities) {
				writer.write(activity);
			}
			writer.endRecord();
		}
		writer.write("always before");
		writer.write("" + allPresets.size());
		writer.endRecord();
		for (String activity : allPresets.keySet()) {
			writer.write(activity);
			for (String activity2 : allPresets.get(activity)) {
				writer.write(activity2);
			}
			writer.endRecord();
		}
		writer.write("always after");
		writer.write("" + allPostsets.size());
		writer.endRecord();
		for (String activity : allPostsets.keySet()) {
			writer.write(activity);
			for (String activity2 : allPostsets.get(activity)) {
				writer.write(activity2);
			}
			writer.endRecord();
		}
		writer.write("sometimes before");
		writer.write("" + anyPresets.size());
		writer.endRecord();
		for (String activity : anyPresets.keySet()) {
			writer.write(activity);
			for (String activity2 : anyPresets.get(activity)) {
				writer.write(activity2);
			}
			writer.endRecord();
		}
		writer.write("sometimes after");
		writer.write("" + anyPostsets.size());
		writer.endRecord();
		for (String activity : anyPostsets.keySet()) {
			writer.write(activity);
			for (String activity2 : anyPostsets.get(activity)) {
				writer.write(activity2);
			}
			writer.endRecord();
		}
		writer.write("required");
		writer.write(required.isEmpty() ? "0" : "1");
		writer.endRecord();
		if (!required.isEmpty()) {
			for (String activity : required) {
				writer.write(activity);
			}
			writer.endRecord();
		}
		writer.write("forbidden");
		writer.write(forbidden.isEmpty() ? "0" : "1");
		writer.endRecord();
		if (!forbidden.isEmpty()) {
			for (String activity : forbidden) {
				writer.write(activity);
			}
			writer.endRecord();
		}
		writer.write("splitters");
		writer.write("" + splitters.size());
		writer.endRecord();
		for (List<String> splitter : splitters) {
			for (String activity : splitter) {
				writer.write(activity);
			}
			writer.endRecord();
		}
		writer.endRecord();
	}

	public void importFromStream(CsvReader reader) throws IOException {
		if (reader.readRecord()) {
			label = reader.get(0);
		}
		sameCounts = new HashSet<Collection<String>>();
		countModel.importFromStream(reader);
		if (reader.readRecord()) {
			if (reader.get(0).equals("always together")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						List<String> orderedActivities = new ArrayList<String>();
						for (int column = 0; column < reader.getColumnCount(); column++) {
							orderedActivities.add(reader.get(column));
						}
						Collections.sort(orderedActivities);
						sameCounts.add(orderedActivities);
					}
				}
			}
		}
		allPresets = new HashMap<String, Set<String>>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("always before")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						Set<String> activities = new HashSet<String>();
						for (int column = 1; column < reader.getColumnCount(); column++) {
							activities.add(reader.get(column));
						}
						allPresets.put(activity, activities);
					}
				}
			}
		}
		allPostsets = new HashMap<String, Set<String>>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("always after")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						Set<String> activities = new HashSet<String>();
						for (int column = 1; column < reader.getColumnCount(); column++) {
							activities.add(reader.get(column));
						}
						allPostsets.put(activity, activities);
					}
				}
			}
		}
		anyPresets = new HashMap<String, Set<String>>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("sometimes before")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						Set<String> activities = new HashSet<String>();
						for (int column = 1; column < reader.getColumnCount(); column++) {
							activities.add(reader.get(column));
						}
						anyPresets.put(activity, activities);
					}
				}
			}
		}
		anyPostsets = new HashMap<String, Set<String>>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("sometimes after")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						Set<String> activities = new HashSet<String>();
						for (int column = 1; column < reader.getColumnCount(); column++) {
							activities.add(reader.get(column));
						}
						anyPostsets.put(activity, activities);
					}
				}
			}
		}
		required = new HashSet<String>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("required")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						for (int column = 0; column < reader.getColumnCount(); column++) {
							required.add(reader.get(column));
						}
					}
				}
			}
		}
		forbidden = new HashSet<String>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("forbidden")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						for (int column = 0; column < reader.getColumnCount(); column++) {
							forbidden.add(reader.get(column));
						}
					}
				}
			}
		}
		splitters = new ArrayList<List<String>>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("splitters")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						List<String> splitter = new ArrayList<String>();
						for (int column = 0; column < reader.getColumnCount(); column++) {
							splitter.add(reader.get(column));
						}
						splitters.add(splitter);
					}
				}
			}
		}
	}
}
