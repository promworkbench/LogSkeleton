package org.processmining.logskeleton.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;
import org.processmining.logskeleton.classifiers.PrefixClassifier;
import org.processmining.logskeleton.configurations.BuilderConfiguration;
import org.processmining.logskeleton.inputs.BuilderInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.outputs.BuilderOutput;

public class BuilderAlgorithm {

	public BuilderOutput apply(PluginContext context, BuilderInput input, BuilderConfiguration configuration) {
		XLog log = input.getLog();

		/*
		 * Create count for entire log.
		 */
		System.out.println("[BuilderAlgorithm] Creating log count.");
		LogSkeletonCount logCount = count(log, configuration);

		/*
		 * Create a count for every trace.
		 */
		System.out.println("[BuilderAlgorithm] Creating trace counts.");
		EventLogArray traceLogs = split(log);
		Collection<LogSkeletonCount> traceCounts = createCounts(traceLogs, configuration);

		/*
		 * Create an initial log skeleton from the log count.
		 */
		LogSkeleton logSkeleton = new LogSkeleton(logCount);
		logSkeleton.setHorizon(configuration.getHorizon());

		/*
		 * Add equivalence classes to the log skeleton based on the trace
		 * counts.
		 */
		System.out.println("[BuilderAlgorithm] Creating equivalence classes.");
		addEquivalenceClasses(traceCounts, logSkeleton);

		/*
		 * Add other relations to the log skeleton.
		 */
		System.out.println("[BuilderAlgorithm] Creating relations.");
		createCausalDependencies(log, configuration, logCount, logSkeleton);

		/*
		 * Set the label of the log skeleton.
		 */
		String label = XConceptExtension.instance().extractName(log);
		logSkeleton.setLabel(label == null ? "<not specified>" : label);

		/*
		 * Return the output.
		 */
		System.out.println("[BuilderAlgorithm] Done.");
		return new BuilderOutput(logSkeleton);
	}

	/**
	 * Returns the count for the provided log using the provided configuration.
	 * 
	 * @param log
	 *            THe provided log.
	 * @param configuration
	 *            The provided configuration.
	 * @return The count.
	 */
	public LogSkeletonCount count(XLog log, BuilderConfiguration configuration) {
		XEventClassifier classifier = new PrefixClassifier(configuration.getClassifier());

		/*
		 * Start a new count.
		 */
		LogSkeletonCount logCount = new LogSkeletonCount();

		/*
		 * Count how often activities and transitions occur in the log.
		 */
		for (XTrace trace : log) {
			String activity;

			/*
			 * Initialize the previous activity.
			 */
			String prevActivity = LogSkeletonCount.STARTEVENT;
			logCount.inc(prevActivity);

			for (XEvent event : trace) {
				activity = classifier.getClassIdentity(event);

				/*
				 * Activity has occurred.
				 */
				logCount.inc(activity);

				/*
				 * transition from previous activity to activity has occurred.
				 */
				logCount.inc(prevActivity, activity);

				prevActivity = activity;
			}
			/*
			 * Finalize the count.
			 */
			activity = LogSkeletonCount.ENDEVENT;
			logCount.inc(activity);
			logCount.inc(prevActivity, activity);
		}
		/*
		 * Return the count.
		 */
		return logCount;
	}

	/*
	 * Splits the log into trace logs (a sublog for every trace).
	 */
	private EventLogArray split(XLog log) {
		EventLogArray traceLogs = EventLogArrayFactory.createEventLogArray();
		for (XTrace trace : log) {
			XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog();
			traceLog.add(trace);
			traceLogs.addLog(traceLogs.getSize(), traceLog);
		}
		return traceLogs;
	}

	/*
	 * Returns the trace counts for the trace logs.
	 */
	private Collection<LogSkeletonCount> createCounts(EventLogArray traceLogs, BuilderConfiguration configuration) {
		Collection<LogSkeletonCount> traceCounts = new ArrayList<LogSkeletonCount>();
		for (int i = 0; i < traceLogs.getSize(); i++) {
			XLog log = traceLogs.getLog(i);
			traceCounts.add(count(log, configuration));
		}
		return traceCounts;
	}

	private void addEquivalenceClasses(Collection<LogSkeletonCount> traceCounts, LogSkeleton logSkeleton) {

		/*
		 * Maps a list of counts (for every trace a count) to the set of
		 * activities that have this list of counts.
		 */
		Map<List<Integer>, Set<String>> traceCountList2Activities = new HashMap<List<Integer>, Set<String>>();

		/*
		 * Get all activities over all trace counts.
		 */
		Set<String> allActivities = new HashSet<String>();
		for (LogSkeletonCount traceCount : traceCounts) {
			allActivities.addAll(traceCount.getActivities());
		}

		/*
		 * For every activity, count how often it occurs in every trace.
		 */
		for (String activity : allActivities) {
			/*
			 * Determine the trace count for this activity.
			 */
			List<Integer> traceCountList = new ArrayList<Integer>();
			for (LogSkeletonCount traceCount : traceCounts) {
				traceCountList.add(traceCount.get(activity));
			}

			/*
			 * Add the trace count to the map.
			 */
			if (traceCountList2Activities.containsKey(traceCountList)) {
				/*
				 * Add activity to existing trace count.
				 */
				traceCountList2Activities.get(traceCountList).add(activity);
			} else {
				/*
				 * Create new trace count for this activity.
				 */
				Set<String> activities = new HashSet<String>();
				activities.add(activity);
				traceCountList2Activities.put(traceCountList, activities);
			}
		}

		/*
		 * We now have the base equivalence classes (the values in the map).
		 * From this, create the equivalence classes for every noise level (0% -
		 * 20%).
		 */
		boolean changed = true;
		int nofTraces = traceCountList2Activities.keySet().isEmpty() ? 0
				: traceCountList2Activities.keySet().iterator().next().size();
		/*
		 * For every noise level, starting at noise level 0.
		 */
		for (int noiseLevel = 0; noiseLevel < 21; noiseLevel++) {
			/*
			 * Create a copy from the map.
			 */
			System.out.println("[BuilderAlgorithm] Creating Equivalence class for noiselevel " + noiseLevel + ".");
			Map<List<Integer>, Set<String>> newTraceCountList2Activities = new HashMap<List<Integer>, Set<String>>();
			for (List<Integer> c : traceCountList2Activities.keySet()) {
				newTraceCountList2Activities.put(c, new HashSet<String>(traceCountList2Activities.get(c)));
			}

			/*
			 * Update the copy: If the increased noise level causes two
			 * equivalence classes to be joined, join them.
			 * 
			 * Keep on going as long as there were changes.
			 */
			while (changed) {
				changed = false;
				/*
				 * No join yet.
				 */
				for (List<Integer> traceCountList1 : newTraceCountList2Activities.keySet()) {
					for (List<Integer> traceCountList2 : newTraceCountList2Activities.keySet()) {
						if (!newTraceCountList2Activities.get(traceCountList1)
								.equals(newTraceCountList2Activities.get(traceCountList2))
								&& 100 * distance(traceCountList1, traceCountList2) < noiseLevel * nofTraces) {
							/*
							 * Distance is too small for noise level. Join both
							 * equivalence classes.
							 */
							newTraceCountList2Activities.get(traceCountList1)
									.addAll(newTraceCountList2Activities.get(traceCountList2));
							newTraceCountList2Activities.get(traceCountList2)
									.addAll(newTraceCountList2Activities.get(traceCountList1));
							changed = true;
						}
					}
				}
			}

			/*
			 * Copy the equivalence classes for this noise level to the log
			 * skeleton.
			 */
			for (Set<String> equivalenceClass : newTraceCountList2Activities.values()) {
				logSkeleton.addEquivalenceClass(noiseLevel, equivalenceClass);
			}

			/*
			 * Replace the original map by the copy.
			 */
			traceCountList2Activities = newTraceCountList2Activities;

			/*
			 * Set to false for next noise level.
			 */
			changed = true;
		}
	}

	/*
	 * Returns the distance between two trace counts.
	 */
	private int distance(List<Integer> traceCountList1, List<Integer> traceCountList2) {
		int distance = 0;
		int size = Math.min(traceCountList1.size(), traceCountList2.size());
		/*
		 * The distance is the accumulation of the activity-wise differences.
		 */
		for (int i = 0; i < size; i++) {
			distance += Math.abs(traceCountList1.get(i) - traceCountList2.get(i));
		}
		return distance;
	}

	/*
	 * Registers the (Not) Response/Precedence and Not Co-Existence in the log
	 * skeleton.
	 */
	private void createCausalDependencies(XLog log, BuilderConfiguration configuration, LogSkeletonCount count,
			LogSkeleton logSkeleton) {
		XEventClassifier classifier = new PrefixClassifier(configuration.getClassifier());
		for (XTrace trace : log) {
			/*
			 * Add the entire trace (including artificial start and end) to the
			 * postset.
			 */
			List<String> postset = new ArrayList<String>();
			postset.add(LogSkeletonCount.STARTEVENT);
			for (XEvent event : trace) {
				postset.add(classifier.getClassIdentity(event));
			}
			postset.add(LogSkeletonCount.ENDEVENT);
			/*
			 * Preset is initially empty.
			 */
			List<String> preset = new ArrayList<String>();
			String prevActivity = null;
			/*
			 * For every activity: register the preset and the postset.
			 */
			while (!postset.isEmpty()) {
				if (prevActivity != null) {
					preset.add(0, prevActivity);
				}
				String activity = postset.remove(0);
				Set<String> filteredPreset = new HashSet<String>();
				for (int i = 0; i < preset.size(); i++) {
					if (i > 0 && i == configuration.getHorizon()) {
						break;
					} else if (configuration.getBoundaryActivities().contains(preset.get(i))) {
						filteredPreset.add(preset.get(i));
						break;
					} else {
						filteredPreset.add(preset.get(i));
					}
				}
				Set<String> filteredPostset = new HashSet<String>();
				for (int i = 0; i < postset.size(); i++) {
					if (i > 0 && i == configuration.getHorizon()) {
						break;
					} else if (configuration.getBoundaryActivities().contains(postset.get(i))) {
						filteredPostset.add(postset.get(i));
						break;
					} else {
						filteredPostset.add(postset.get(i));
					}
				}
				logSkeleton.addPrePost(activity,  filteredPreset, filteredPostset);
//				if (configuration.getHorizon() > 0) {
//					/*
//					 * Use horizon: Use only the first X activities in the
//					 * preset and the postset, where X is the current horizon.
//					 */
//					Set<String> horizonPreset = new HashSet<String>();
//					Set<String> horizonPostset = new HashSet<String>();
//					for (int i = 0; i < configuration.getHorizon(); i++) {
//						if (i < preset.size()) {
//							horizonPreset.add(preset.get(i));
//						}
//						if (i < postset.size()) {
//							horizonPostset.add(postset.get(i));
//						}
//					}
//					System.out.println("[BuilderAlgorithm] " + activity + ", " + horizonPreset + ", " + horizonPostset);
//					logSkeleton.addPrePost(activity, horizonPreset, horizonPostset);
//				} else {
//					logSkeleton.addPrePost(activity, preset, postset);
//				}
				prevActivity = activity;
			}
		}
		/*
		 * Clean up.
		 */
		logSkeleton.cleanPrePost();
	}
}
