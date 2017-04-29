package org.processmining.logskeleton.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.HTMLToString;
import org.processmining.logskeleton.parameters.ViewLogSkeleton;
import org.processmining.logskeleton.parameters.ViewLogSkeletonParameters;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

public class LogSkeleton implements HTMLToString {

	private LogSkeletonCount countModel;
	private LogSkeletonCount correctedCountModel;
	private Collection<Collection<String>> sameCounts;
	private Map<String, Set<String>> allPresets;
	private Map<String, Set<String>> allPostsets;
	private Map<String, Set<String>> anyPresets;
	private Map<String, Set<String>> anyPostsets;
	private Map<List<String>, List<Integer>> distances;

	public LogSkeleton(LogSkeletonCount countModel, LogSkeletonCount correctedCountModel) {
		this.countModel = countModel;
		this.correctedCountModel = correctedCountModel;
		sameCounts = new HashSet<Collection<String>>();
		allPresets = new HashMap<String, Set<String>>();
		allPostsets = new HashMap<String, Set<String>>();
		anyPresets = new HashMap<String, Set<String>>();
		anyPostsets = new HashMap<String, Set<String>>();
		distances = new HashMap<List<String>, List<Integer>>();
	}

	public void addSameCount(Collection<String> activities) {
		sameCounts.add(activities);
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
		int distance = 1;
		for (String postActivity : post) {
			List<String> pair = new ArrayList<String>();
			pair.add(activity);
			pair.add(postActivity);
			if (!distances.containsKey(pair)) {
				distances.put(pair, new ArrayList<Integer>());
			}
			distances.get(pair).add(distance);
			distance++;
		}
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

	private boolean checkSameCounts(LogSkeletonCount model) {
		boolean ok = true;
		for (Collection<String> sameCount : sameCounts) {
			Set<Integer> counts = new HashSet<Integer>();
			for (String activity : sameCount) {
				counts.add(model.get(activity));
			}
			if (counts.size() != 1) {
				System.out.println("[PDC2017ConstraintModel] Check failed: " + sameCount);
				ok = false;
			}
		}
		return ok;
	}

	private boolean checkTransitionCounts(LogSkeletonCount model) {
		return countModel.checkTransitionCounts(model);
	}

	private boolean checkValueInterval(int valueToCheck, List<Integer> values) {
		int min = Integer.MAX_VALUE;
		int max = 0;
		double sumValues = 0.0;
		double n = values.size();
		if (n < 50.0) {
			return true;
		}
		for (Integer value : values) {
			min = Math.min(min, value);
			max = Math.max(max, value);
			sumValues += value;
		}
		double avgValue = ((1.0) * sumValues) / n;
		double sumDiffs = 0.0;
		for (Integer value : values) {
			sumDiffs += (value - avgValue) * (value - avgValue);
		}
		double stdDev = Math.sqrt(sumDiffs / (n - 1));
		double confLevel90 = 1.645;
		double confLevel95 = 1.96;
		double confLevel99 = 2.575;
		double halfInterval = confLevel99 * stdDev / Math.sqrt(n);
		if (!(min - halfInterval <= valueToCheck && valueToCheck <= max + halfInterval)) {
			System.out.println("[PDC2017ConstraintModel] " + valueToCheck + " in [" + (min - halfInterval) + ","
					+ (max + halfInterval) + "] " + values);
		}
		return min - halfInterval <= valueToCheck && valueToCheck <= max + halfInterval;
	}

	private boolean checkDistance(int distance, List<Integer> distances) {
		if (distance > 0) {
			return true;
		}
		if (distance > 2 || distances.size() < 10) {
			return true;
		}
		int min = Integer.MAX_VALUE;
		int max = 0;
		for (Integer value : distances) {
			min = Math.min(min, value);
			max = Math.max(max, value);
		}
		if (min > distance || distance > max) {
			System.out.println("[PDC2017ConstraintModel] " + distance + " " + distances);
		}
		return min <= distance && distance <= max;
	}

	private boolean checkCausalDependencies(XTrace trace) {
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
				int distance = 1;
				for (String postActivity : postset) {
					List<String> pair = new ArrayList<String>();
					pair.add(prevActivity);
					pair.add(postActivity);
					if (distances.containsKey(pair) && !checkDistance(distance, distances.get(pair))) {
						//						return false;
					}
					distance++;
				}
			}
			String activity = postset.remove(0);
			if (!preset.containsAll(allPresets.get(activity))) {
				return false;
			}
			if (!postset.containsAll(allPostsets.get(activity))) {
				return false;
			}
			if (!anyPresets.get(activity).containsAll(preset)) {
				return false;
			}
			if (!anyPostsets.get(activity).containsAll(postset)) {
				return false;
			}
			prevActivity = activity;
		}
		return true;
	}

	public boolean check(XTrace trace, LogSkeletonCount model) {
		boolean ok = true;
		ok = ok && checkSameCounts(model);
		//		ok = ok && checkPlaceCounts(model);
		//		ok = ok && checkPlaceRelations(trace, model);
		ok = ok && checkCausalDependencies(trace);
		ok = ok && checkTransitionCounts(model);
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

	public Dot visualize(ViewLogSkeletonParameters parameters) {
		Map<String, DotNode> map = new HashMap<String, DotNode>();
		Dot graph = new Dot();
//		System.out.println("[PDC2017ConstrainModel] Activities = " + parameters.getActivities());
//		System.out.println("[PDC2017ConstrainModel] Visualizers = " + parameters.getVisualizers());
		for (String activity : parameters.getActivities()) {
			map.put(activity, graph.addNode(activity + "\n" + countModel.get(activity)));
		}
		for (ViewLogSkeleton visualizer : parameters.getVisualizers()) {
			switch (visualizer) {
				case ALWAYSTOGETHER : {
					for (Collection<String> siblings : sameCounts) {
						for (String fromActivity : siblings) {
							if (parameters.getActivities().contains(fromActivity)) {
								for (String toActivity : siblings) {
									if (parameters.getActivities().contains(toActivity)) {
										if (fromActivity.compareTo(toActivity) > 0) {
											DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
											arc.setOption("dir", "both");
											arc.setOption("arrowtail", "obox");
											arc.setOption("arrowhead", "obox");
										}
									}
								}
							}
						}
					}
					break;
				}
				case ALWAYSBEFORE : {
					for (String toActivity : allPresets.keySet()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : allPresets.get(toActivity)) {
								if (parameters.getActivities().contains(fromActivity)) {
									DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
									arc.setOption("dir", "both");
									arc.setOption("arrowtail", "none");
									arc.setOption("arrowhead", "oboxnormal");
								}
							}
						}
					}
					break;
				}
				case ALWAYSAFTER : {
					for (String toActivity : allPostsets.keySet()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : allPostsets.get(toActivity)) {
								if (parameters.getActivities().contains(fromActivity)) {
									DotEdge arc = graph.addEdge(map.get(toActivity), map.get(fromActivity));
									arc.setOption("dir", "both");
									arc.setOption("arrowtail", "obox");
									arc.setOption("arrowhead", "normal");
								}
							}
						}
					}
					break;
				}
				case NEVERTOGETHER : {
					for (String fromActivity : countModel.getActivities()) {
						if (parameters.getActivities().contains(fromActivity)) {
							for (String toActivity : countModel.getActivities()) {
								if (parameters.getActivities().contains(toActivity)) {
									if (fromActivity.compareTo(toActivity) >= 0
											&& !anyPresets.get(fromActivity).contains(toActivity)
											&& !anyPostsets.get(fromActivity).contains(toActivity)) {
										DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
										arc.setOption("dir", "both");
										arc.setOption("arrowtail", "box");
										arc.setOption("arrowhead", "box");
									}
								}
							}
						}
					}
					break;
				}
				case NEVERBEFORE : {
					for (String toActivity : anyPresets.keySet()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : countModel.getActivities()) {
								if (parameters.getActivities().contains(fromActivity)) {
									if (!anyPresets.get(toActivity).contains(fromActivity)
											&& anyPostsets.get(toActivity).contains(fromActivity)) {
										DotEdge arc = graph.addEdge(map.get(toActivity), map.get(fromActivity));
										arc.setOption("dir", "both");
										arc.setOption("arrowtail", "normal");
										arc.setOption("arrowhead", "box");
									}
								}
							}
						}
					}
					break;
				}
				case NEVERAFTER : {
					for (String toActivity : anyPostsets.keySet()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : countModel.getActivities()) {
								if (parameters.getActivities().contains(fromActivity)) {
									if (!anyPostsets.get(toActivity).contains(fromActivity)
											&& anyPresets.get(toActivity).contains(fromActivity)) {
										DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
										arc.setOption("dir", "both");
										arc.setOption("arrowtail", "boxnormal");
										arc.setOption("arrowhead", "none");
									}
								}
							}
						}
					}
					break;
				}
				case SOMETIMESBEFORE : {
					for (String toActivity : anyPresets.keySet()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : countModel.getActivities()) {
								if (parameters.getActivities().contains(fromActivity)) {
									if (anyPresets.get(toActivity).contains(fromActivity)) {
										DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
										arc.setOption("dir", "both");
										arc.setOption("arrowtail", "none");
										arc.setOption("arrowhead", "odiamondnormal");
									}
								}
							}
						}
					}
					break;
				}
				case SOMETIMESAFTER : {
					for (String toActivity : anyPostsets.keySet()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : countModel.getActivities()) {
								if (parameters.getActivities().contains(fromActivity)) {
									if (anyPostsets.get(toActivity).contains(fromActivity)) {
										DotEdge arc = graph.addEdge(map.get(toActivity), map.get(fromActivity));
										arc.setOption("dir", "both");
										arc.setOption("arrowtail", "odiamond");
										arc.setOption("arrowhead", "normal");
									}
								}
							}
						}
					}
					break;
				}
				case NEXTONEWAY : {
					for (String toActivity : countModel.getActivities()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : countModel.getActivities()) {
								if (parameters.getActivities().contains(fromActivity)) {
									if (countModel.get(fromActivity, toActivity) > 0
											&& countModel.get(toActivity, fromActivity) == 0) {
										DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
										arc.setOption("dir", "both");
										arc.setOption("arrowtail", "odot");
										arc.setOption("arrowhead", "normal");
										arc.setLabel("" + countModel.get(fromActivity, toActivity));
									}
								}
							}
						}
					}
					break;
				}
				case NEXTBOTHWAYS : {
					for (String toActivity : countModel.getActivities()) {
						if (parameters.getActivities().contains(toActivity)) {
							for (String fromActivity : countModel.getActivities()) {
								if (parameters.getActivities().contains(fromActivity)) {
									if (fromActivity.compareTo(toActivity) >= 0
											&& (countModel.get(fromActivity, toActivity) > 0 && countModel.get(
													toActivity, fromActivity) > 0)) {
										DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
										arc.setOption("dir", "both");
										arc.setOption("arrowtail", "odotvee");
										arc.setOption("arrowhead", "odotnormal");
										arc.setLabel("" + countModel.get(fromActivity, toActivity) + "/"
												+ countModel.get(toActivity, fromActivity));
									}
								}
							}
						}
					}
					break;
				}
			}
		}
		return graph;
	}

	public Dot createGraph(ViewLogSkeleton visualizer) {
		ViewLogSkeletonParameters parameters = new ViewLogSkeletonParameters();
		parameters.getActivities().addAll(countModel.getActivities());
		parameters.getVisualizers().add(visualizer);
		return visualize(parameters);
	}

	public Dot createGraph(Set<ViewLogSkeleton> visualizers) {
		ViewLogSkeletonParameters parameters = new ViewLogSkeletonParameters();
		parameters.getActivities().addAll(countModel.getActivities());
		parameters.getVisualizers().addAll(visualizers);
		return visualize(parameters);
	}

	public Dot createGraph(ViewLogSkeletonParameters parameters) {
		parameters.getActivities().addAll(parameters.getActivities());
		parameters.getVisualizers().addAll(parameters.getVisualizers());
		return visualize(parameters);
	}

	public Collection<String> getActivities() {
		return countModel.getActivities();
	}
}
