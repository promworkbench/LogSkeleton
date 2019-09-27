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
		LogSkeletonCount countModel = count(log, configuration);
		countModel.print("Count model");
		EventLogArray logs = split(log);
		Collection<LogSkeletonCount> counts = createCounts(logs, configuration);
		LogSkeleton constraintModel = new LogSkeleton(countModel);
		addEquivalenceClasses(counts, constraintModel);
		createCausalDependencies(log, configuration, countModel, constraintModel);
		String label = XConceptExtension.instance().extractName(log);
		constraintModel.setLabel(label == null ? "<not specified>" : label);
		return new BuilderOutput(constraintModel);
	}

	public LogSkeletonCount count(XLog log, BuilderConfiguration configuration) {
		XEventClassifier classifier = new PrefixClassifier(configuration.getClassifier());
		LogSkeletonCount model = new LogSkeletonCount();
		for (XTrace trace : log) {
			String activity;
			String prevActivity = LogSkeletonCount.STARTEVENT;
			model.inc(prevActivity);
			for (XEvent event : trace) {
				activity = classifier.getClassIdentity(event);
				model.inc(activity);
				model.inc(prevActivity, activity);
				prevActivity = activity;
			}
			activity = LogSkeletonCount.ENDEVENT;
			model.inc(activity);
			model.inc(prevActivity, activity);
		}
		return model;
	}

	private EventLogArray split(XLog log) {
		int N = log.size();
		EventLogArray logs = EventLogArrayFactory.createEventLogArray();
		for (int i = 0; i < N; i++) {
			logs.addLog(i, XFactoryRegistry.instance().currentDefault().createLog());
		}
		int i = 0;
		for (XTrace trace : log) {
			logs.getLog(i).add(trace);
			i++;
			if (i == N) {
				i = 0;
			}
		}
		return logs;
	}

	private Collection<LogSkeletonCount> createCounts(EventLogArray logs, BuilderConfiguration configuration) {
		Collection<LogSkeletonCount> models = new ArrayList<LogSkeletonCount>();
		for (int i = 0; i < logs.getSize(); i++) {
			XLog log = logs.getLog(i);
			models.add(count(log, configuration));
		}
		return models;
	}

	private void addEquivalenceClasses(Collection<LogSkeletonCount> countModels, LogSkeleton constraintModel) {
		Map<List<Integer>, Set<String>> map = new HashMap<List<Integer>, Set<String>>();
		Set<String> activities = new HashSet<String>();
		for (LogSkeletonCount countModel : countModels) {
			activities.addAll(countModel.getActivities());
		}
		for (String activity : activities) {
			List<Integer> count = new ArrayList<Integer>();
			for (LogSkeletonCount countModel : countModels) {
				count.add(countModel.get(activity));
			}
			if (map.containsKey(count)) {
				map.get(count).add(activity);
			} else {
				Set<String> newCount = new HashSet<String>();
				newCount.add(activity);
				map.put(count, newCount);
			}
		}

		boolean changed = true;
		int size = map.keySet().isEmpty() ? 0 : map.keySet().iterator().next().size();
		for (int noiseLevel = 0; noiseLevel < 21; noiseLevel++) {
			Map<List<Integer>, Set<String>> map2 = new HashMap<List<Integer>, Set<String>>();
			for (List<Integer> c : map.keySet()) {
				map2.put(c, new HashSet<String>(map.get(c)));
			}
			while (changed) {
				changed = false;
				for (List<Integer> c1 : map2.keySet()) {
					for (List<Integer> c2 : map2.keySet()) {
						if (!map2.get(c1).equals(map2.get(c2)) && 100 * distance(c1, c2) < noiseLevel * size) {
							map2.get(c1).addAll(map2.get(c2));
							map2.get(c2).addAll(map2.get(c1));
							changed = true;
						}
					}
				}
			}

			for (Set<String> equivalenceClass : map2.values()) {
				constraintModel.addEquivalenceClass(noiseLevel, equivalenceClass);
			}
			map = map2;
			changed = true;
		}
	}

	private int distance(List<Integer> c1, List<Integer> c2) {
		int distance = 0;
		int size = Math.min(c1.size(), c2.size());
		for (int i = 0; i < size; i++) {
			distance += Math.abs(c1.get(i) - c2.get(i));
		}
		return distance;
	}

	private void createCausalDependencies(XLog log, BuilderConfiguration configuration, LogSkeletonCount model, LogSkeleton constraintModel) {
		XEventClassifier classifier = new PrefixClassifier(configuration.getClassifier());
		for (XTrace trace : log) {
			List<String> postset = new ArrayList<String>();
			postset.add(LogSkeletonCount.STARTEVENT);
			for (XEvent event : trace) {
				postset.add(classifier.getClassIdentity(event));
			}
			postset.add(LogSkeletonCount.ENDEVENT);
			Set<String> preset = new HashSet<String>();
			String prevActivity = null;
			while (!postset.isEmpty()) {
				if (prevActivity != null) {
					preset.add(prevActivity);
				}
				String activity = postset.remove(0);
				constraintModel.addPrePost(activity, preset, postset);
				prevActivity = activity;
			}
		}
		constraintModel.cleanPrePost();
	}
}
