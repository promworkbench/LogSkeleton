package org.processmining.logskeleton.models;

import java.awt.Color;
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

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.annotations.AuthoredType;
import org.processmining.framework.annotations.Icon;
import org.processmining.framework.util.HTMLToString;
import org.processmining.logskeleton.classifiers.PrefixClassifier;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.configurations.CheckerConfiguration;
import org.processmining.logskeleton.models.violations.ViolationEquivalence;
import org.processmining.logskeleton.models.violations.ViolationNotPrecedence;
import org.processmining.logskeleton.models.violations.ViolationNotResponse;
import org.processmining.logskeleton.models.violations.ViolationPrecedence;
import org.processmining.logskeleton.models.violations.ViolationResponse;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

@AuthoredType(typeName = "Log skeleton", affiliation = AuthoredType.TUE, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
@Icon(icon = "rotule_30x35.png")
public class LogSkeleton implements HTMLToString {

	/*
	 * Holds the counters and the directly-follows relation.
	 */
	private LogSkeletonCount countModel;

	/*
	 * The Equivalence relation per equivalence threshold. If 0 <= L <= N and S is
	 * an element of equivalances.get(L), then all elements of S are equivalent at
	 * noise level L.
	 */
	private Map<Integer, Collection<Collection<String>>> equivalenceClasses;

	/*
	 * The Precedence relation. If precedence.get(a).contains(b), then if a occurs,
	 * some b must occur before.
	 */
	private Map<String, ThresholdSet> precedences;

	/*
	 * The Response relation. If response.get(a).contains(b), then if a occurs, some
	 * b must occur after.
	 */
	private Map<String, ThresholdSet> responses;

	/*
	 * The Not Precedence relation. If notPrecedence.get(a).contains(b), then if a
	 * occurs, no b may occur before. As a result, if both a and b occur, then b
	 * occurs after a.
	 */
	private Map<String, ThresholdSet> notPrecedences;

	/*
	 * The Not Response relation. If notResponse.get(a).contains(b), then if a
	 * occurs, no b may occur after. As a result, if both a and b occur, then b
	 * occurs before a.
	 */
	private Map<String, ThresholdSet> notResponses;

	/*
	 * The Not Co-Existence relation. If notCoExistence.get(a).contains(b), then if
	 * a occurs, b may not occur (before or after).
	 */
	private Map<String, ThresholdSet> notCoExistences;

	/*
	 * Filters and splitters. Used for the legend below the log skeleton.
	 */
	private Set<String> required;
	private Set<String> forbidden;
	private Set<String> boundary;
	private List<List<String>> splitters;

	/*
	 * Label for the log skeleton.
	 */
	private String label;

	/*
	 * Horizon for the log skeleton.
	 */
	private int horizon;

	/*
	 * Thresholds for the log skeleton. A threshold of X corresponds to a noise
	 * level of 100-X. As a result, the threshold 100 allows for no noise, 99 allows
	 * for minimal noise, etc.
	 */
	private int equivalenceThreshold;
	private int precedenceThreshold;
	private int responseThreshold;
	private int notCoExistenceeThreshold;

	public LogSkeleton() {
		this(new LogSkeletonCount());
	}

	public LogSkeleton(LogSkeletonCount countModel) {
		this.countModel = countModel;
		equivalenceClasses = new HashMap<Integer, Collection<Collection<String>>>();
		for (int noiseLevel = 0; noiseLevel < 21; noiseLevel++) {
			equivalenceClasses.put(noiseLevel, new HashSet<Collection<String>>());
		}
		precedences = new HashMap<String, ThresholdSet>();
		responses = new HashMap<String, ThresholdSet>();
		notPrecedences = new HashMap<String, ThresholdSet>();
		notResponses = new HashMap<String, ThresholdSet>();
		notCoExistences = new HashMap<String, ThresholdSet>();
		required = new HashSet<String>();
		forbidden = new HashSet<String>();
		boundary = new HashSet<String>();
		splitters = new ArrayList<List<String>>();
		label = null;
		setEquivalenceThreshold(100);
		setPrecedenceThreshold(100);
		setResponseThreshold(100);
		setNotCoExistenceThreshold(100);
		setHorizon(0);
	}

	/*
	 * @deprecated Use addEquivalenceClass instead.
	 */
	@Deprecated
	public void addSameCount(Collection<String> activities) {
		addEquivalenceClass(activities);
	}

	/**
	 * Adds the provided activities as an equivalence for the current noise level.
	 * 
	 * @param activities
	 *            The provided activities
	 */
	public void addEquivalenceClass(Collection<String> activities) {
		List<String> equivalenceClass = new ArrayList<String>(activities);
		Collections.sort(equivalenceClass);
		equivalenceClasses.get(100 - equivalenceThreshold).add(equivalenceClass);
	}

	/*
	 * @deprecated Use addEquivalenceClass instead.
	 */
	@Deprecated
	public void addSameCount(int noiseLevel, Collection<String> activities) {
		addEquivalenceClass(noiseLevel, activities);
	}

	/**
	 * Adds the provided activities as an equivalence for the provided noise level.
	 * 
	 * @param noiseLevel
	 *            The provided noise level.
	 * @param activities
	 *            The provided activities.
	 */
	public void addEquivalenceClass(int noiseLevel, Collection<String> activities) {
		List<String> equivalenceClass = new ArrayList<String>(activities);
		Collections.sort(equivalenceClass);
		equivalenceClasses.get(noiseLevel).add(equivalenceClass);
	}

	/*
	 * @deprecated Use getEquivalenceClass instead.
	 */
	@Deprecated
	public Collection<String> getSameCounts(String activity) {
		return getEquivalenceClass(activity);
	}

	/**
	 * Returns the equivalence class for the provided activity for the current
	 * threshold.
	 * 
	 * @param activity
	 *            The provided activity.
	 * @return The equivalence class.
	 */
	public Collection<String> getEquivalenceClass(String activity) {
		for (Collection<String> equivalenceClass : equivalenceClasses.get(100 - equivalenceThreshold)) {
			if (equivalenceClass.contains(activity)) {
				return equivalenceClass;
			}
		}
		return null;
	}

	/*
	 * @deprecated Use getEquivalenceClass instead.
	 */
	@Deprecated
	public Collection<String> getSameCounts(String activity, Collection<String> activities) {
		return getEquivalenceClass(activity, activities);
	}

	/**
	 * Returns the intersection of the provided set of activities and the
	 * equivalence class for the provided activity for the current threshold.
	 * 
	 * This is useful if the representative of an equivalence class is not shown. As
	 * a result, the smallest shown activity will be used as representative.
	 * 
	 * @param activity
	 *            The provided activity.
	 * @param activities
	 *            The provided activities.
	 * @return The intersected equivalence class.
	 */
	public Collection<String> getEquivalenceClass(String activity, Collection<String> activities) {
		for (Collection<String> equivalenceClass : equivalenceClasses.get(100 - equivalenceThreshold)) {
			if (equivalenceClass.contains(activity)) {
				Collection<String> filteredEquivalenceClass = new HashSet<String>(equivalenceClass);
				filteredEquivalenceClass.retainAll(activities);
				return filteredEquivalenceClass;
			}
		}
		return null;
	}

	/**
	 * Registers that the provided activity has the provided preset of activities
	 * and the provided postset of activities.
	 * 
	 * @param activity
	 *            The provided activity.
	 * @param pre
	 *            The provided preset of activities. These activities occur before
	 *            the provided activity in the trace.
	 * @param post
	 *            The provided postset of activities. These activities occur after
	 *            the provided activity in the trace.
	 */
	public void addPrePost(String activity, Collection<String> pre, Collection<String> post) {
		Set<String> preset = new HashSet<String>(pre);
		Set<String> postset = new HashSet<String>(post);
		/*
		 * Initialization if a new activity.
		 */
		if (!precedences.containsKey(activity)) {
			precedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
		}
		if (!responses.containsKey(activity)) {
			responses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
		}
		if (!notPrecedences.containsKey(activity)) {
			notPrecedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
		}
		if (!notResponses.containsKey(activity)) {
			notResponses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
		}
		if (!notCoExistences.containsKey(activity)) {
			notCoExistences.put(activity, new ThresholdSet(countModel.getActivities(), notCoExistenceeThreshold));
		}
		/*
		 * Add all activities from the preset to the precedences.
		 */
		precedences.get(activity).addAll(preset);
		/*
		 * Add all activities from the postset to the responses.
		 */
		responses.get(activity).addAll(postset);
		/*
		 * Add all activities not in the preset to the notPrecedences.
		 */
		Set<String> negPreset = new HashSet<String>(countModel.getActivities());
		negPreset.removeAll(preset);
		notPrecedences.get(activity).addAll(negPreset);
		/*
		 * Add all activities not in the postset to the notResponses.
		 */
		Set<String> negPostset = new HashSet<String>(countModel.getActivities());
		negPostset.removeAll(postset);
		notResponses.get(activity).addAll(negPostset);
		/*
		 * Add all activities not in the preset and not in the postset to the
		 * notCoExistences.
		 */
		Set<String> prepostset = new HashSet<String>(countModel.getActivities());
		prepostset.removeAll(preset);
		prepostset.removeAll(postset);
		notCoExistences.get(activity).addAll(prepostset);
	}

	/**
	 * Restore any removed activities in the relations.
	 * 
	 * Removing an activity A from a (threshold!) set S removes it in the sense that
	 * S.contains(A) will return false. However, the activity will still be there in
	 * another way, which allows us to restore the removal.
	 */
	public void cleanPrePost() {
		for (String activity : precedences.keySet()) {
			precedences.get(activity).reset();
		}
		for (String activity : responses.keySet()) {
			responses.get(activity).reset();
		}
		for (String activity : notPrecedences.keySet()) {
			notPrecedences.get(activity).reset();
		}
		for (String activity : notResponses.keySet()) {
			notResponses.get(activity).reset();
		}
		for (String activity : notCoExistences.keySet()) {
			notPrecedences.get(activity).removeAll(notCoExistences.get(activity));
			notResponses.get(activity).removeAll(notCoExistences.get(activity));
		}
	}

	/**
	 * Returns a set of activities to which the relation is redundant. The relation
	 * R is redundant for the provided activity A and a returned activity C if there
	 * is third activity B such that (A,C) in R, (A,B) in R, and (B,C) in R. The
	 * activities A, B, and C should all be part of the provided set of activities.
	 * 
	 * @param activity
	 *            The activity A
	 * @param relation
	 *            The relation R
	 * @param activities
	 *            The provided activities (to take into account).
	 * @return The set of possible activities C.
	 */
	private Set<String> getRedundant(String activity, Map<String, ThresholdSet> relation,
			Collection<String> activities) {
		Set<String> redundantActivities = new HashSet<String>();
		for (String activity2 : relation.get(activity)) {
			if (activities.contains(activity2)) {
				for (String activity3 : relation.get(activity2)) {
					if (activities.contains(activity3)) {
						if (relation.get(activity).contains(activity3)
						/* && !relation.get(activity3).contains(activity2) */) {
							redundantActivities.add(activity3);
						}
					}
				}
			}
		}
		return redundantActivities;
	}

	/**
	 * Checks whether the provided count model violates the equivalence classes of
	 * this count model using the provided configuration.
	 * 
	 * @param model
	 *            The provided count model
	 * @param configuration
	 *            The provided configuration
	 * @param trace
	 *            The trace to report if violations are detected
	 * @return A collection of violations. Depending on the configuration, only the
	 *         first violation is returned, or all violations.
	 */
	private Collection<Violation> checkEquivalenceClasses(LogSkeletonCount model, CheckerConfiguration configuration,
			XTrace trace) {
		Set<Violation> violations = new HashSet<Violation>();
		for (Collection<String> equivalenceClass : equivalenceClasses.get(100 - equivalenceThreshold)) {
			Set<Integer> counts = new HashSet<Integer>();
			for (String activity : equivalenceClass) {
				counts.add(model.get(activity));
			}
			if (counts.size() != 1) {
				/*
				 * Equivalent activities should have the same count for every trace.
				 */
				violations.add(new ViolationEquivalence(trace, new HashSet<String>(equivalenceClass)));
				if (configuration.isStopAtFirstViolation()) {
					return violations;
				}
			}
		}
		return violations;
	}

	/**
	 * Checks whether the provided count model violates the transition counts of
	 * this count model using the provided configuration.
	 * 
	 * Most notably, this checks whether the provided count model contains a
	 * transition that is not contained in this count model.
	 * 
	 * @param model
	 *            The provided count model.
	 * @param configuration
	 *            The provided configuration.
	 * @param trace
	 *            The trace to report if violations are detected.
	 * @return A collection of violations. Depending on the configuration, only the
	 *         first violation is returned, or all violations.
	 */
	private Collection<Violation> checkTransitionCounts(LogSkeletonCount model, CheckerConfiguration configuration,
			XTrace trace) {
		return countModel.checkTransitionCounts(model, configuration, trace);
	}

	/**
	 * Checks whether the provided trace violates the (not) response/precedence
	 * relations of this log skeleton.
	 * 
	 * @param trace
	 *            The provided trace.
	 * @param configuration
	 *            The provided configuration.
	 * @return A collection of violations. Depending on the configuration, only the
	 *         first violation is returned, or all violations.
	 */
	private Collection<Violation> checkCausalDependencies(XTrace trace, CheckerConfiguration configuration) {
		XEventClassifier classifier = new PrefixClassifier(configuration.getClassifier());
		List<String> postset = new ArrayList<String>();
		postset.add(LogSkeletonCount.STARTEVENT);
		Set<Violation> violations = new HashSet<Violation>();
		for (XEvent event : trace) {
			postset.add(classifier.getClassIdentity(event));
		}
		postset.add(LogSkeletonCount.ENDEVENT);
		List<String> preset = new ArrayList<String>();
		String prevActivity = null;
		while (!postset.isEmpty()) {
			if (prevActivity != null) {
				preset.add(prevActivity);
			}
			String activity = postset.remove(0);
			/*
			 * preset contains all activities that precede activity in the trace. postset
			 * contains all activities that succeed activity in the trace.
			 */

			/*
			 * Check whether all precedences are in the preset.
			 */
			if (precedences.containsKey(activity) && !preset.containsAll(precedences.get(activity))) {
				/*
				 * Some precedences are not in the preset. Report them as violations.
				 */
				Set<String> missing = new HashSet<String>(precedences.get(activity));
				missing.removeAll(preset);
				violations.add(new ViolationPrecedence(trace, activity, missing));
				if (configuration.isStopAtFirstViolation()) {
					return violations;
				}
			}
			/*
			 * Check whether all responses are in the postset.
			 */
			if (responses.containsKey(activity) && !postset.containsAll(responses.get(activity))) {
				/*
				 * Some responses are not in the postset. Report them as violations.
				 */
				Set<String> missing = new HashSet<String>(responses.get(activity));
				missing.removeAll(postset);
				violations.add(new ViolationResponse(trace, activity, missing));
				if (configuration.isStopAtFirstViolation()) {
					return violations;
				}
			}
			/*
			 * Check whether no not-precedences are in the preset.
			 */
			Set<String> notPreset = new HashSet<String>(countModel.getActivities());
			notPreset.removeAll(preset);
			if (notPrecedences.containsKey(activity) && !notPreset.containsAll(notPrecedences.get(activity))) {
				/*
				 * Some not-precedences are in the preset. Report them as violations.
				 */
				Set<String> present = new HashSet<String>(notPrecedences.get(activity));
				present.removeAll(notPreset);
				violations.add(new ViolationNotPrecedence(trace, activity, present));
				if (configuration.isStopAtFirstViolation()) {
					return violations;
				}
			}
			/*
			 * Check whether no not-responses are in the postset.
			 */
			Set<String> notPostset = new HashSet<String>(countModel.getActivities());
			notPostset.removeAll(postset);
			if (notResponses.containsKey(activity) && !notPostset.containsAll(notResponses.get(activity))) {
				/*
				 * Some not-responses are in the postset. Report them as violations.
				 */
				Set<String> present = new HashSet<String>(notResponses.get(activity));
				present.removeAll(notPostset);
				violations.add(new ViolationNotResponse(trace, activity, present));
				if (configuration.isStopAtFirstViolation()) {
					return violations;
				}
			}
			prevActivity = activity;
		}
		return violations;
	}

	/**
	 * Checks whether the provided trace and the provided count model violate this
	 * log skeleton or this count model, using the provided configuration.
	 * 
	 * @param trace
	 *            The provided trace.
	 * @param model
	 *            The provided count model.
	 * @param configuration
	 *            The provided configuration.
	 * @return A collection of violations. The configuration determines which checks
	 *         are done. Depending on the configuration, only the first violation is
	 *         returned, or all violations.
	 */
	public Collection<Violation> check(XTrace trace, LogSkeletonCount model, CheckerConfiguration configuration) {
		boolean[] checks = configuration.getChecks();
		Set<Violation> violations = new HashSet<Violation>();

		if (checks[0]) {
			/*
			 * Check equivalence classes.
			 */
			violations.addAll(checkEquivalenceClasses(model, configuration, trace));
			if (configuration.isStopAtFirstViolation() && !violations.isEmpty()) {
				return violations;
			}
		}
		if (checks[1]) {
			/*
			 * Check (not) response/precedence relations.
			 */
			violations.addAll(checkCausalDependencies(trace, configuration));
			if (configuration.isStopAtFirstViolation() && !violations.isEmpty()) {
				return violations;
			}
		}
		if (checks[2]) {
			/*
			 * Check activity and transition counts.
			 */
			violations.addAll(checkTransitionCounts(model, configuration, trace));
			if (configuration.isStopAtFirstViolation() && !violations.isEmpty()) {
				return violations;
			}
		}
		return violations;
	}

	/**
	 * Returns the log skeleton as an HTML string. For debugging purposes only.
	 */
	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buf = new StringBuffer();
		List<String> sorted;
		if (includeHTMLTags) {
			buf.append("<html>");
		}
		buf.append("<h1>Causal relations</h1><table>");
		buf.append(
				"<tr><th>Activity</th><th>Sibling activities</th><th>Count</th><th>Precedence</th><th>Response</th><th>Not co-occurrence</th></tr>");
		for (String activity : countModel.getActivities()) {
			// Activity
			buf.append("<tr><td>" + activity + "</td>");
			// Sibling activities and count
			for (Collection<String> siblings : equivalenceClasses.get(100 - equivalenceThreshold)) {
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
			// Precedence
			sorted = new ArrayList<String>(precedences.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Response
			sorted = new ArrayList<String>(responses.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Not co-occurrence
			sorted = new ArrayList<String>(countModel.getActivities());
			sorted.removeAll(notCoExistences.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
		}
		buf.append("</table>");
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

	/**
	 * Returns a dot visualization of the log skeleton, using the provided
	 * configuration.
	 * 
	 * @param configuration
	 *            The provided configuration.
	 * @return The dot visualization.
	 */
	public Dot visualize(BrowserConfiguration configuration) {
		Map<String, DotNode> map = new HashMap<String, DotNode>();
		Dot graph = new Dot();

		/*
		 * Create a color scheme (based on Set312) containing 99 different colors
		 * (including gradients). Color 100 is white and is used as fallback color.
		 */
		String[] set312Colors = new String[] { "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462",
				"#b3de69", "#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f" };
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

		/*
		 * Initialization
		 */
		int colorIndex = 0;
		Map<String, String> colorMap = new HashMap<String, String>();

		/*
		 * Get all selected activities. Retain only those that are actually present.
		 */
		Set<String> activities = new HashSet<String>(configuration.getActivities());
		activities.retainAll(countModel.getActivities());

		/*
		 * Copy the thresholds.
		 */
		setPrecedenceThreshold(configuration.getPrecedenceThreshold());
		setResponseThreshold(configuration.getResponseThreshold());
		setNotCoExistenceThreshold(configuration.getNotCoExistenceThreshold());
		setEquivalenceThreshold(configuration.getEquivalenceThreshold());

		System.out.println("[LogSkeleton] Extending activities");

		if (configuration.isUseNeighbors()) {
			/*
			 * Extend the selected activities with activities that are related (through some
			 * selected non-redundant relation) to a selected activity.
			 */
			for (String fromActivity : countModel.getActivities()) {
				for (String toActivity : countModel.getActivities()) {
					if ((configuration.getActivities().contains(fromActivity)
							|| configuration.getActivities().contains(toActivity))
							&& (!activities.contains(fromActivity) || !activities.contains(toActivity))) {
						if (configuration.getRelations().contains(LogSkeletonRelation.RESPONSE)) {
							if (responses.get(fromActivity).contains(toActivity)
									&& !getRedundant(fromActivity, responses, countModel.getActivities())
											.contains(toActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Response relation, and one of them is selected. Include the other as well.
								 */
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.PRECEDENCE)) {
							if (precedences.get(toActivity).contains(fromActivity)
									&& !getRedundant(toActivity, precedences, countModel.getActivities())
											.contains(fromActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Precedence relation, and one of them is selected. Include the other as well.
								 */
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.NOTRESPONSE)) {
							if (notResponses.get(toActivity).contains(fromActivity)
									&& !getRedundant(toActivity, notResponses, countModel.getActivities())
											.contains(fromActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Not Response relation, and one of them is selected. Include the other as
								 * well.
								 */
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.NOTPRECEDENCE)) {
							if (notPrecedences.get(fromActivity).contains(toActivity)
									&& !getRedundant(fromActivity, notPrecedences, countModel.getActivities())
											.contains(toActivity)) {
								/*
								 * fromActivity and toActivity are related through the selected non-redundant
								 * Not Precedence relation, and one of them is selected. Include the other as
								 * well.
								 */
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						if (configuration.getRelations().contains(LogSkeletonRelation.NOTCOEXISTENCE)) {
							if (!fromActivity.equals(toActivity)) {
								if (fromActivity.compareTo(toActivity) >= 0
										&& (!configuration.isUseEquivalenceClass() || fromActivity
												.equals(getEquivalenceClass(fromActivity, countModel.getActivities())
														.iterator().next()))
										&& (!configuration.isUseEquivalenceClass() || toActivity
												.equals(getEquivalenceClass(toActivity, countModel.getActivities())
														.iterator().next()))
										&& notCoExistences.get(fromActivity).contains(toActivity)) {
									/*
									 * fromActivity and toActivity are related through the selected non-redundant
									 * Not Co-Existence relation, and one of them is selected. Include the other as
									 * well.
									 */
									activities.add(fromActivity);
									activities.add(toActivity);
								}
							}
						}
					}
				}
			}
		}

		System.out.println("[LogSkeleton] Creating activities");
		/*
		 * Create a dot node for every (included) activity.
		 */
		for (String activity : activities) {
			/*
			 * Get a color for this activity.
			 */
			String representative = getEquivalenceClass(activity, activities).iterator().next();
			String representativeColor = colorMap.get(representative);
			if (representativeColor == null) {
				representativeColor = colors[colorIndex];
				colorMap.put(representative, representativeColor);
				if (colorIndex < colors.length - 1) {
					colorIndex++;
				}
			}
			/*
			 * Get the count interval for this activity.
			 */
			String interval = "" + countModel.getMin(activity);
			if (countModel.getMax(activity) > countModel.getMin(activity)) {
				interval += ".." + countModel.getMax(activity);
			}
			/*
			 * Determine the border width for this activity: - 1 if selected - 0 if not
			 * selected (but included).
			 * 
			 */
			int border = 0;
			if (configuration.getActivities().contains(activity)) {
				border = 1;
			}

			/*
			 * Create the dot node.
			 */
			System.out.println("[LogSkeleton] Set node label to " + activity);
			DotNode node = graph.addNode("<<table align=\"center\" bgcolor=\"" + representativeColor + "\" border=\""
					+ border
					+ "\" cellborder=\"0\" cellpadding=\"2\" columns=\"*\" style=\"rounded\"><tr><td colspan=\"3\"><font point-size=\"24\"><b>"
					+ encodeHTML(activity) + "</b></font></td></tr><hr/><tr><td>" + representative + "</td><td>"
					+ countModel.get(activity) + "</td>" + "<td>" + interval + "</td>" + "</tr></table>>");
			node.setOption("shape", "none");
			if (!configuration.getFontname().isEmpty()) {
				node.setOption("fontname", configuration.getFontname());
			}
			map.put(activity, node);
		}

		/*
		 * Initialize colors for the relations. Lighter colors are used if a relation
		 * only holds if some noise is permitted.
		 */
		/*
		 * For edges without a relation.
		 */
		String defaultColor = darker("#d9d9d9");
		/*
		 * For edges with a Non Co-Existence relation.
		 */
		String lighterNotCoExistenceColor = "#fdb462";
		String notCoExistenceColor = darker(lighterNotCoExistenceColor);
		/*
		 * For edges with a Response or Precedence relation.
		 */
		String lighterResponsePrecedenceColor = "#80b1d3";
		String responsePrecedenceColor = darker(lighterResponsePrecedenceColor);
		/*
		 * For edges with a Not Response or Not Precedence relation.
		 */
		String lighterNotResponsePrecedenceColor = "#fb8072";
		String notResponsePrecedenceColor = darker(lighterNotResponsePrecedenceColor);

		System.out.println("[LogSkeleton] Creating relations");

		for (String fromActivity : activities) {
			for (String toActivity : activities) {
				if (configuration.getActivities().contains(fromActivity)
						|| configuration.getActivities().contains(toActivity)) {
					String tailDecorator = null;
					String headDecorator = null;
					String tailLabel = null;
					String headLabel = null;
					String tailArrow = null;
					String headArrow = null;
					String headColor = null;
					String tailColor = null;
					boolean isAsymmetric = true;
					if (configuration.getRelations().contains(LogSkeletonRelation.RESPONSE)) {
						if (tailDecorator == null && responses.get(fromActivity).contains(toActivity)
								&& !getRedundant(fromActivity, responses, activities).contains(toActivity)) {
							/*
							 * Show Response relation.
							 */
							tailDecorator = "noneinv";
							tailColor = responsePrecedenceColor;
							int threshold = responses.get(fromActivity).getMaxThreshold(toActivity);
							if (threshold < 100) {
								tailLabel = "." + threshold;
								tailColor = lighterResponsePrecedenceColor;
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.PRECEDENCE)) {
						if (headDecorator == null && precedences.get(toActivity).contains(fromActivity)
								&& !getRedundant(toActivity, precedences, activities).contains(fromActivity)) {
							/*
							 * Show Precedence relation.
							 */
							headDecorator = "normal";
							headColor = responsePrecedenceColor;
							int threshold = precedences.get(toActivity).getMaxThreshold(fromActivity);
							if (threshold < 100) {
								headLabel = "." + threshold;
								headColor = lighterResponsePrecedenceColor;
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.NOTCOEXISTENCE)) {
						if (!fromActivity.equals(toActivity)) {
							if (headDecorator == null && fromActivity.compareTo(toActivity) >= 0
									&& (!configuration.isUseEquivalenceClass() || fromActivity
											.equals(getEquivalenceClass(fromActivity, activities).iterator().next()))
									&& (!configuration.isUseEquivalenceClass() || toActivity
											.equals(getEquivalenceClass(toActivity, activities).iterator().next()))
									&& notCoExistences.get(toActivity).contains(fromActivity)) {
								boolean doShow = configuration.isUseNCEReductions()
										? showNotCoExistence(fromActivity, toActivity, activities)
										: true;
								if (doShow) {
									/*
									 * Show Not Co-Existence relation.
									 */
									headDecorator = "nonetee";
									isAsymmetric = false;
									headColor = notCoExistenceColor;
									int threshold = notCoExistences.get(toActivity).getMaxThreshold(fromActivity);
									if (threshold < 100) {
										headLabel = "." + threshold;
										headColor = lighterNotCoExistenceColor;
									}
								}
							}
							if (tailDecorator == null && fromActivity.compareTo(toActivity) >= 0
									&& (!configuration.isUseEquivalenceClass() || fromActivity
											.equals(getEquivalenceClass(fromActivity, activities).iterator().next()))
									&& (!configuration.isUseEquivalenceClass() || toActivity
											.equals(getEquivalenceClass(toActivity, activities).iterator().next()))
									&& notCoExistences.get(fromActivity).contains(toActivity)) {
								boolean doShow = configuration.isUseNCEReductions()
										? showNotCoExistence(fromActivity, toActivity, activities)
										: true;
								if (doShow) {
									/*
									 * Show Not Co-Existence relation.
									 */
									tailDecorator = "nonetee";
									isAsymmetric = false;
									tailColor = notCoExistenceColor;
									int threshold = notCoExistences.get(fromActivity).getMaxThreshold(toActivity);
									if (threshold < 100) {
										tailLabel = "." + threshold;
										tailColor = lighterNotCoExistenceColor;
									}
								}
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.NOTRESPONSE)) {
						if (!fromActivity.equals(toActivity) && headDecorator == null
								&& notResponses.get(toActivity).contains(fromActivity)
								&& !getRedundant(toActivity, notResponses, activities).contains(fromActivity)) {
							/*
							 * Show Not Response relation.
							 */
							if (configuration.isUseInvertedArrows()) {
								headDecorator = "noneinvtee";
							} else {
								headDecorator = "onormal";
							}
							headColor = notResponsePrecedenceColor;
							int threshold = notResponses.get(toActivity).getMaxThreshold(fromActivity);
							if (threshold < 100) {
								headLabel = "." + threshold;
								headColor = lighterNotResponsePrecedenceColor;
							}
						}
					}
					if (configuration.getRelations().contains(LogSkeletonRelation.NOTPRECEDENCE)) {
						if (!fromActivity.equals(toActivity) && tailDecorator == null
								&& notPrecedences.get(fromActivity).contains(toActivity)
								&& !getRedundant(fromActivity, notPrecedences, activities).contains(toActivity)) {
							/*
							 * Show Not Precedence relation.
							 */
							if (configuration.isUseInvertedArrows()) {
								tailDecorator = "teenormal";
							} else {
								tailDecorator = "noneoinv";
							}
							tailColor = notResponsePrecedenceColor;
							int threshold = notPrecedences.get(fromActivity).getMaxThreshold(toActivity);
							if (threshold < 100) {
								tailLabel = "." + threshold;
								tailColor = lighterNotResponsePrecedenceColor;
							}
						}
					}
					if (tailDecorator != null || headDecorator != null || tailArrow != null || headArrow != null) {
						/*
						 * Some relation should be shown. Create a corresponding edge.
						 */
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
						if (!configuration.getFontname().isEmpty()) {
							arc.setOption("fontname", configuration.getFontname());
						}

						if (configuration.isUseFalseConstraints() && !isAsymmetric) {
							/*
							 * Ignore symmetric relations in the layout.
							 */
							arc.setOption("constraint", "false");
						}

						if (configuration.isUseEdgeColors() && (headColor != null || tailColor != null)) {
							/*
							 * Color the edges.
							 */
							String color = (tailColor == null ? defaultColor : tailColor) + ";0.5:"
									+ (headColor == null ? defaultColor : headColor) + ";0.5";
							arc.setOption("color", color);
						}

						if (configuration.isUseHeadTailLabels()) {
							/*
							 * Show labels seperatley at head/tail
							 */
							if (headLabel != null) {
								arc.setOption("headlabel", headLabel);
							}
							if (tailLabel != null) {
								arc.setOption("taillabel", tailLabel);
							}
						} else if (headLabel != null || tailLabel != null) {
							/*
							 * Show labels combined at middle of arc
							 */
							String label = "";
							if (tailLabel != null) {
								label += tailLabel;
							}
							label += "&rarr;";
							if (headLabel != null) {
								label += headLabel;
							}
							arc.setLabel(label);
						}
					}
				}
			}
		}

		System.out.println("[LogSkeleton] Creating hyper-relations");

		if (configuration.isUseHyperArcs()) {
			/*
			 * Replaces cliques of edges by a hyper edge.
			 */

			/*
			 * Sort the edge to get a (more) deterministic result.
			 */
			List<DotEdge> candidateEdges = new ArrayList<DotEdge>(graph.getEdges());
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
						for (DotEdge anotherEdge : graph.getEdges()) {
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
					Set<DotEdge> edges = getMaximalClique(graph, sourceNodes, targetNodes, edge.getOption("arrowtail"),
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
						DotNode connector = graph.addNode("");
						connector.setOption("shape", "point");
						/*
						 * Add edges from and to the new connector node.
						 */
						for (DotNode node : sourceNodes) {
							DotEdge anotherEdge = graph.addEdge(node, connector);
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
									System.out.println("[LogSkeleton] set color1 " + colors[0]);
									anotherEdge.setOption("color", colors2[0]);
								} else {
									anotherEdge.setOption("color", edge.getOption("color"));
								}
							}
							candidateEdges.add(anotherEdge);
						}
						for (DotNode node : targetNodes) {
							DotEdge anotherEdge = graph.addEdge(connector, node);
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
									System.out.println("[LogSkeleton] set color2 " + colors[2]);
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
							graph.removeEdge(anotherArc);
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

		System.out.println("[LogSkeleton] Creating legend");

		/*
		 * Add a legend to the dot visualization.
		 */
		graph.setOption("labelloc", "b");
		graph.setOption("nodesep", "0.5");
		if (!configuration.getFontname().isEmpty()) {
			System.out.println("[LogSkeleton] fontname = " + configuration.getFontname());
			graph.setOption("fontname", configuration.getFontname());
		}
		if (!configuration.getFontnameRepresentation().isEmpty()) {
			System.out.println("[LogSkeleton] fontnames = " + configuration.getFontnameRepresentation());
			graph.setOption("fontnames", configuration.getFontnameRepresentation());
		}
		List<String> selectedActivities = new ArrayList<String>(configuration.getActivities());
		Collections.sort(selectedActivities);
		String label = "<table bgcolor=\"gold\" cellborder=\"0\" cellpadding=\"0\" columns=\"3\" style=\"rounded\">";
		label += encodeHeader("Skeleton configuration");
		/*
		 * Show name of log skeleton (that is, the name of the log).
		 */
		label += encodeRow("Event Log", this.label == null ? "<not specified>" : this.label);
		if (!required.isEmpty()) {
			/*
			 * List required activities.
			 */
			label += encodeRow("Required activities", required.toString());
		}
		if (!forbidden.isEmpty()) {
			/*
			 * List forbidden activities
			 */
			label += encodeRow("Forbidden activities", forbidden.toString());
		}
		if (!boundary.isEmpty()) {
			/*
			 * List boundary activities
			 */
			label += encodeRow("Boundary activities", boundary.toString());
		}
		if (!splitters.isEmpty()) {
			/*
			 * List splitters.
			 */
			label += encodeRow("Splitters", splitters.toString());
		}
		/*
		 * List selected activities.
		 */
		label += encodeRow("View activities", selectedActivities.toString());
		/*
		 * List selected relations.
		 */
		label += encodeRow("View relations", configuration.getRelations().toString());
		/*
		 * Show horizon if not 0.
		 */
		if (horizon > 0) {
			label += encodeRow("Horizon", "" + horizon);
		}
		/*
		 * List noise levels for thresholds which are not set to 100.
		 */
		if (equivalenceThreshold < 100 || responseThreshold < 100 || precedenceThreshold < 100
				|| notCoExistenceeThreshold < 100) {
			String s = "";
			String d = "";
			if (equivalenceThreshold < 100) {
				s += d + "Equivalence = " + (100 - equivalenceThreshold) + "%";
				d = ", ";
			}
			if (responseThreshold < 100) {
				s += d + "(Not) Response = " + (100 - responseThreshold) + "%";
				d = ", ";
			}
			if (precedenceThreshold < 100) {
				s += d + "(Not) Precedence = " + (100 - precedenceThreshold) + "%";
				d = ", ";
			}
			if (notCoExistenceeThreshold < 100) {
				s += d + "Not Co-Existence = " + (100 - notCoExistenceeThreshold) + "%";
				d = ", ";
			}
			label += encodeRow("Noise levels", s);
		}
		label += "</table>";
		graph.setOption("fontsize", "8.0");
		graph.setOption("label", "<" + label + ">");

		/*
		 * All done. Return this dot visualization.
		 */
		System.out.println("[LogSkeleton] Done");
		return graph;
	}

	private boolean showNotCoExistence(String fromActivity, String toActivity, Set<String> activities) {
		for (String activity : precedences.get(fromActivity)) {
//			String rep = getEquivalenceClass(activity, activities).iterator().next();
//			if (rep.equals(fromActivity)) {
//				break;
//			}
			if (notCoExistences.get(activity).contains(toActivity)) {
				return false;
			}
			if (notCoExistences.get(toActivity).contains(activity)) {
				return false;
			}
		}
		for (String activity : precedences.get(toActivity)) {
//			String rep = getEquivalenceClass(activity, activities).iterator().next();
//			if (rep.equals(toActivity)) {
//				break;
//			}
			if (notCoExistences.get(activity).contains(fromActivity)) {
				return false;
			}
			if (notCoExistences.get(fromActivity).contains(activity)) {
				return false;
			}
		}
//		for (String activity : responses.get(fromActivity)) {
//			String rep = getEquivalenceClass(activity, activities).iterator().next();
//			if (rep.equals(fromActivity)) {
//				break;
//			}
//			if (notCoExistences.get(activity).contains(toActivity)) {
//				return false;
//			}
//			if (notCoExistences.get(toActivity).contains(activity)) {
//				return false;
//			}
//		}
//		for (String activity : responses.get(toActivity)) {
//			String rep = getEquivalenceClass(activity, activities).iterator().next();
//			if (rep.equals(toActivity)) {
//				break;
//			}
//			if (notCoExistences.get(activity).contains(fromActivity)) {
//				return false;
//			}
//			if (notCoExistences.get(fromActivity).contains(activity)) {
//				return false;
//			}
//		}
		return true;
	}

	/*
	 * Returns a color which is slightly darker than the provided color.
	 */
	private String darker(String color) {
		Color darkerColor = Color.decode(color).darker();
		return "#" + Integer.toHexString(darkerColor.getRed()) + Integer.toHexString(darkerColor.getGreen())
				+ Integer.toHexString(darkerColor.getBlue());
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

	/**
	 * Returns a dot visualization for all activities and the provided relation
	 * only.
	 * 
	 * @param relation
	 *            The provided relation.
	 * @return A dot visualization.
	 */
	public Dot createGraph(LogSkeletonRelation relation) {
		BrowserConfiguration configuration = new BrowserConfiguration(null);
		configuration.getActivities().addAll(countModel.getActivities());
		configuration.getRelations().add(relation);
		return visualize(configuration);
	}

	/**
	 * Returns a dot visualization for all activities and the provided relations
	 * only.
	 * 
	 * @param relations
	 *            The provided relations.
	 * @return A dot visualization.
	 */
	public Dot createGraph(Set<LogSkeletonRelation> relations) {
		BrowserConfiguration configuration = new BrowserConfiguration(null);
		configuration.getActivities().addAll(countModel.getActivities());
		configuration.getRelations().addAll(relations);
		return visualize(configuration);
	}

	/**
	 * Returns a dot visualization for the provided configuration.
	 * 
	 * @param configuration
	 *            The provided configuration.
	 * @return A dot visualization.
	 */
	public Dot createGraph(BrowserConfiguration configuration) {
		return visualize(configuration);
	}

	/**
	 * Returns a collection containing all activities.
	 * 
	 * @return A collection containing all activities.
	 */
	public Collection<String> getActivities() {
		return countModel.getActivities();
	}

	/**
	 * Sets the set of required activities (to be included in the legend).
	 * 
	 * @param required
	 *            The set of required activities.
	 */
	public void setRequired(Set<String> required) {
		this.required = required;
	}

	/**
	 * Sets the set of forbidden activities (to be included in the legend).
	 * 
	 * @param forbidden
	 *            The set of forbidden activities.
	 */
	public void setForbidden(Set<String> forbidden) {
		this.forbidden = forbidden;
	}

	/**
	 * Sets the set of boundary activities (to be included in the legend).
	 * 
	 * @param forbidden
	 *            The set of forbidden activities.
	 */
	public void setBoundary(Set<String> boundary) {
		this.boundary = boundary;
	}

	/**
	 * Sets the set of splitters (to be included in the legend).
	 * 
	 * @param splitters
	 *            The set of splitters.
	 */
	public void setSplitters(List<List<String>> splitters) {
		this.splitters = splitters;
	}

	/**
	 * Returns the label for this log skeleton.
	 * 
	 * @return The label for this log skeleton.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label for this log skeleton (to be included in the legend and in new
	 * windows showing log skeletons).
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Exports the log skeleton to a file.
	 * 
	 * @param writer
	 *            The write to use when writing to the file.
	 * @throws IOException
	 */
	public void exportToFile(CsvWriter writer) throws IOException {
		writer.write(label);
		writer.endRecord();
		countModel.exportToFile(writer);
		writer.write("equivalence");
		for (int noise = 0; noise < 21; noise++) {
			writer.write("" + equivalenceClasses.get(noise).size());
		}
		for (int noise = 0; noise < 21; noise++) {
			writer.endRecord();
			for (Collection<String> activities : equivalenceClasses.get(noise)) {
				for (String activity : activities) {
					writer.write(activity);
				}
				writer.endRecord();
			}
		}
		writer.write("precedence");
		writer.write("" + precedences.size());
		writer.endRecord();
		for (String activity : precedences.keySet()) {
			writer.write(activity);
			precedences.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("response");
		writer.write("" + responses.size());
		writer.endRecord();
		for (String activity : responses.keySet()) {
			writer.write(activity);
			responses.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("not precedence");
		writer.write("" + notPrecedences.size());
		writer.endRecord();
		for (String activity : notPrecedences.keySet()) {
			writer.write(activity);
			notPrecedences.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("not response");
		writer.write("" + notResponses.size());
		writer.endRecord();
		for (String activity : notResponses.keySet()) {
			writer.write(activity);
			notResponses.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("not co-occurrence");
		writer.write("" + notCoExistences.size());
		writer.endRecord();
		for (String activity : notCoExistences.keySet()) {
			writer.write(activity);
			notCoExistences.get(activity).exportToFile(writer);
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
		writer.write("horizon");
		writer.write("" + horizon);
		writer.endRecord();
	}

	public void importFromStream(CsvReader reader) throws IOException {
		if (reader.readRecord()) {
			label = reader.get(0);
		}
		equivalenceClasses = new HashMap<Integer, Collection<Collection<String>>>();
		for (int noise = 0; noise < 21; noise++) {
			equivalenceClasses.put(noise, new HashSet<Collection<String>>());
		}
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
						for (int noise = 0; noise < 21; noise++) {
							equivalenceClasses.get(noise).add(orderedActivities);
						}
					}
				}
			} else if (reader.get(0).equals("equivalence")) {
				int rows[] = new int[21];
				for (int noise = 0; noise < 21; noise++) {
					rows[noise] = Integer.valueOf(reader.get(noise + 1));
				}
				for (int noise = 0; noise < 21; noise++) {
					for (int row = 0; row < rows[noise]; row++) {
						if (reader.readRecord()) {
							List<String> orderedActivities = new ArrayList<String>();
							for (int column = 0; column < reader.getColumnCount(); column++) {
								orderedActivities.add(reader.get(column));
							}
							Collections.sort(orderedActivities);
							equivalenceClasses.get(noise).add(orderedActivities);
						}
					}
				}
			}
		}
		precedences = new HashMap<String, ThresholdSet>();
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
						precedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
						precedences.get(activity).addAll(activities);
					}
				}
			} else if (reader.get(0).equals("precedence")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						precedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
						precedences.get(activity).importFromFile(reader);
					}
				}
			}
		}
		responses = new HashMap<String, ThresholdSet>();
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
						responses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
						responses.get(activity).addAll(activities);
					}
				}
			} else if (reader.get(0).equals("response")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						responses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
						responses.get(activity).importFromFile(reader);
					}
				}
			}
		}
		notPrecedences = new HashMap<String, ThresholdSet>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("not precedence")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						notPrecedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
						notPrecedences.get(activity).importFromFile(reader);
					}
				}
			}
		}
		notResponses = new HashMap<String, ThresholdSet>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("not response")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						notResponses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
						notResponses.get(activity).importFromFile(reader);
					}
				}
			}
		}
		if (reader.readRecord()) {
			if (reader.get(0).equals("sometimes before")) {
				Map<String, Set<String>> anyPresets = new HashMap<String, Set<String>>();
				Map<String, Set<String>> anyPostsets = new HashMap<String, Set<String>>();
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
				if (reader.readRecord()) {
					if (reader.get(0).equals("sometimes after")) {
						rows = Integer.valueOf(reader.get(1));
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
				notCoExistences = new HashMap<String, ThresholdSet>();
				for (String activity : countModel.getActivities()) {
					Set<String> prepostset = new HashSet<String>();
					if (anyPresets.containsKey(activity)) {
						prepostset.addAll(anyPresets.get(activity));
					}
					if (anyPostsets.containsKey(activity)) {
						prepostset.addAll(anyPostsets.get(activity));
					}
					notCoExistences.put(activity,
							new ThresholdSet(countModel.getActivities(), notCoExistenceeThreshold));
					notCoExistences.get(activity).addAll(countModel.getActivities());
					notCoExistences.get(activity).removeAll(prepostset);
				}
			} else if (reader.get(0).equals("not co-occurrence")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						notCoExistences.put(activity,
								new ThresholdSet(countModel.getActivities(), notCoExistenceeThreshold));
						notCoExistences.get(activity).importFromFile(reader);
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

		if (reader.readRecord()) {
			if (reader.get(0).equals("horizon")) {
				horizon = Integer.valueOf(reader.get(1));
			}
		}
	}

	/**
	 * Sets the precedence threshold to the provided threshold.
	 * 
	 * @param precedenceThreshold
	 *            The provided threshold.
	 */
	public void setPrecedenceThreshold(int precedenceThreshold) {
		this.precedenceThreshold = precedenceThreshold;
		/*
		 * Copy the threshold into all (not) precedence relations. This threshold may
		 * affect whether contains returns true or false.
		 */
		for (String activity : precedences.keySet()) {
			precedences.get(activity).setThreshold(precedenceThreshold);
		}
		for (String activity : notPrecedences.keySet()) {
			notPrecedences.get(activity).setThreshold(precedenceThreshold);
		}
	}

	/**
	 * Sets the response threshold to the provided threshold.
	 * 
	 * @param responseThreshold
	 *            The provided threshold.
	 */
	public void setResponseThreshold(int responseThreshold) {
		this.responseThreshold = responseThreshold;
		/*
		 * Copy the threshold into all (not) response relations. This threshold may
		 * affect whether contains returns true or false.
		 */
		for (String activity : responses.keySet()) {
			responses.get(activity).setThreshold(responseThreshold);
		}
		for (String activity : notResponses.keySet()) {
			notResponses.get(activity).setThreshold(responseThreshold);
		}
	}

	/**
	 * Sets the not-co-existence threshold to the provided threshold.
	 * 
	 * @param notCoOccurencethreshold
	 *            The provided threshold.
	 */
	public void setNotCoExistenceThreshold(int notCoOccurencethreshold) {
		this.notCoExistenceeThreshold = notCoOccurencethreshold;
		/*
		 * Copy the threshold into all Not Co-Existence relations. This threshold may
		 * affect whether contains returns true or false.
		 */
		for (String activity : notCoExistences.keySet()) {
			notCoExistences.get(activity).setThreshold(notCoOccurencethreshold);
		}
	}

	/**
	 * Returns whether the log skeleton contains more than 100 Not Co-Existence
	 * relations to show. if so, this relation will not be shown by default.
	 * 
	 * @param isUseEquivalenceClass
	 *            Whether the Not Co-Existence relation is restricted to the
	 *            representatives of the Equivalence relation.
	 * @return Whether the number of Not Co-Existence relation exceeds 100.
	 */
	public boolean hasManyNotCoExistenceArcs(boolean isUseEquivalenceClass) {
		int nr = 0;
		for (String fromActivity : countModel.getActivities()) {
			for (String toActivity : countModel.getActivities()) {
				if (!fromActivity.equals(toActivity)) {
					if (fromActivity.compareTo(toActivity) >= 0
							&& (!isUseEquivalenceClass
									|| fromActivity.equals(getEquivalenceClass(fromActivity).iterator().next()))
							&& (!isUseEquivalenceClass
									|| toActivity.equals(getEquivalenceClass(toActivity).iterator().next()))
							&& notCoExistences.get(fromActivity).contains(toActivity)) {
						nr++;
					}
				}
			}
		}
		/*
		 * Return whether there are too many Not Co-Existence constraints to show by
		 * default. THe first visualization should be reasonably fast. In case of too
		 * many Not Co-Existence constraints, this first visualization takes ages.
		 */
		return nr > 100;
	}

	/**
	 * Sets the equivalence threshold to the provided threshold.
	 * 
	 * @param equivalenceThreshold
	 *            The provided threshold.
	 */
	public void setEquivalenceThreshold(int equivalenceThreshold) {
		this.equivalenceThreshold = equivalenceThreshold;
	}

	public void setHorizon(int horizon) {
		this.horizon = horizon;
	}
}
