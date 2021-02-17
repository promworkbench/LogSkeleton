package org.processmining.logskeleton.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.processmining.logskeleton.algorithms.GraphBuilderAlgorithm;
import org.processmining.logskeleton.algorithms.GraphVisualizerAlgorithm;
import org.processmining.logskeleton.classifiers.PrefixClassifier;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.configurations.CheckerConfiguration;
import org.processmining.logskeleton.models.violations.ViolationEquivalence;
import org.processmining.logskeleton.models.violations.ViolationNotPrecedence;
import org.processmining.logskeleton.models.violations.ViolationNotResponse;
import org.processmining.logskeleton.models.violations.ViolationPrecedence;
import org.processmining.logskeleton.models.violations.ViolationResponse;
import org.processmining.plugins.graphviz.dot.Dot;

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
		System.out.println("[LogSkeleton] violations: " + violations);
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
		GraphBuilderAlgorithm builder = new GraphBuilderAlgorithm();
		LogSkeletonGraph graph = builder.apply(this,  configuration);
		GraphVisualizerAlgorithm visualizer = new GraphVisualizerAlgorithm();
		return visualizer.apply(graph, configuration);
	}
	
	/*
	 * Returns whether the Not Co-Existence relation from fromActivity to toActivity
	 * should be shown.
	 */
	public boolean showNotCoExistence(String fromActivity, String toActivity, Set<String> activities,
			Set<String> selectedActivities) {
		for (String activity : precedences.get(fromActivity)) {
			if (selectedActivities.contains(activity)) {
				if (notCoExistences.get(activity).contains(toActivity)) {
					return false;
				}
				if (notCoExistences.get(toActivity).contains(activity)) {
					return false;
				}
			}
		}
		for (String activity : precedences.get(toActivity)) {
			if (selectedActivities.contains(activity)) {
				if (notCoExistences.get(activity).contains(fromActivity)) {
					return false;
				}
				if (notCoExistences.get(fromActivity).contains(activity)) {
					return false;
				}
			}
		}
		return true;
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

	public int getPrecedenceThreshold() {
		return precedenceThreshold;
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

	public int getResponseThreshold() {
		return responseThreshold;
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

	public int getNotCoExistenceThreshold() {
		return notCoExistenceeThreshold;
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

	public int getEquivalenceThreshold() {
		return equivalenceThreshold;
	}
	
	public void setHorizon(int horizon) {
		this.horizon = horizon;
	}

	public boolean hasNonRedundantResponse(String fromActivity, String toActivity) {
		return responses.get(fromActivity).contains(toActivity)
				&& !getRedundant(fromActivity, responses, countModel.getActivities()).contains(toActivity);
	}

	public boolean hasNonRedundantPrecedence(String fromActivity, String toActivity) {
		return precedences.get(toActivity).contains(fromActivity)
				&& !getRedundant(toActivity, precedences, countModel.getActivities()).contains(fromActivity);
	}

	public boolean hasNonRedundantNotResponse(String fromActivity, String toActivity) {
		return notResponses.get(toActivity).contains(fromActivity)
				&& !getRedundant(toActivity, notResponses, countModel.getActivities()).contains(fromActivity);
	}

	public boolean hasNonRedundantNotPrecedence(String fromActivity, String toActivity) {
		return notPrecedences.get(fromActivity).contains(toActivity)
				&& !getRedundant(fromActivity, notPrecedences, countModel.getActivities()).contains(toActivity);
	}

	public boolean hasNotResponse(String fromActivity, String toActivity) {
		return notResponses.get(toActivity).contains(fromActivity);
	}

	public boolean hasNotPrecedence(String fromActivity, String toActivity) {
		return notPrecedences.get(fromActivity).contains(toActivity);
	}

	public boolean hasNonRedundantNotCoExistence(String fromActivity, String toActivity,
			BrowserConfiguration configuration) {
		return !fromActivity.equals(toActivity) 
				&& (!configuration.isUseEquivalenceClass() || fromActivity
						.equals(getEquivalenceClass(fromActivity, countModel.getActivities()).iterator().next()))
				&& (!configuration.isUseEquivalenceClass() || toActivity
						.equals(getEquivalenceClass(toActivity, countModel.getActivities()).iterator().next()))
				&& notCoExistences.get(fromActivity).contains(toActivity);

	}

	public int getMin(String activity) {
		return countModel.getMin(activity);
	}

	public int getMax(String activity) {
		return countModel.getMax(activity);
	}

	public int getCount(String activity) {
		return countModel.get(activity);
	}

	public int getMaxThresholdResponse(String fromActivity, String toActivity) {
		return responses.get(fromActivity).getMaxThreshold(toActivity);
	}

	public int getMaxThresholdPrecedence(String fromActivity, String toActivity) {
		return precedences.get(toActivity).getMaxThreshold(fromActivity);
	}

	public int getMaxThresholdNotResponse(String fromActivity, String toActivity) {
		return notResponses.get(toActivity).getMaxThreshold(fromActivity);
	}

	public int getMaxThresholdNotPrecedence(String fromActivity, String toActivity) {
		return notPrecedences.get(fromActivity).getMaxThreshold(toActivity);
	}

	public int getMaxThresholdNotCoExistence(String fromActivity, String toActivity) {
		return notCoExistences.get(toActivity).getMaxThreshold(fromActivity);
	}

	public Set<String> getRequired() {
		return required;
	}

	public Set<String> getForbidden() {
		return forbidden;
	}

	public Set<String> getBoundary() {
		return boundary;
	}

	public List<List<String>> getSplitters() {
		return splitters;
	}
	
	public int getHorizon() {
		return horizon;
	}
}
