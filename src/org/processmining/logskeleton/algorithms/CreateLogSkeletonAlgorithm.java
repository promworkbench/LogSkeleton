package org.processmining.logskeleton.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;

public class CreateLogSkeletonAlgorithm {

	public LogSkeleton apply(XLog log) {
		LogSkeletonCount countModel0 = count(log);
		//		countModel0.print("Count model");
		LogSkeletonCount countModel1 = correct(countModel0);
		//		countModel1.print("Par model");
		LogSkeletonCount countModel2 = correct(log, countModel1);
		LogSkeletonCount countModel = correct2(countModel2);
		countModel = countModel0;
		countModel.print("Count model");
		EventLogArray logs = split(log);
		Collection<LogSkeletonCount> counts = createCounts(logs);
		LogSkeleton constraintModel = new LogSkeleton(countModel, countModel2);
		addSameCounts(counts, constraintModel);
		createCausalDependencies(log, countModel, constraintModel);
		return constraintModel;
	}

	public LogSkeletonCount count(XLog log) {
		LogSkeletonCount model = new LogSkeletonCount();

		for (XTrace trace : log) {
			String activity;
			String prevActivity = LogSkeletonCount.STARTEVENT;
			model.inc(prevActivity);
			for (XEvent event : trace) {
				activity = XConceptExtension.instance().extractName(event);
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

	public LogSkeletonCount correct(XLog log, LogSkeletonCount model) {
		LogSkeletonCount correctedModel = new LogSkeletonCount();
		for (XTrace trace : log) {
			String activity;
			List<String> prevActivities = new ArrayList<String>();
			prevActivities.add(LogSkeletonCount.STARTEVENT);
			correctedModel.inc(LogSkeletonCount.STARTEVENT);
			for (XEvent event : trace) {
				activity = XConceptExtension.instance().extractName(event);
				correctedModel.inc(activity);
				int n = prevActivities.size();
				String prevActivity = prevActivities.get(--n);
				while (n > 0 && prevActivity != null && model.get(prevActivity, activity) == 0) {
					prevActivity = prevActivities.get(--n);
				}
				if (prevActivity != null) {
					correctedModel.inc(prevActivity, activity);
				}
				prevActivity = activity;
				prevActivities.add(prevActivity);
			}
			activity = LogSkeletonCount.ENDEVENT;
			correctedModel.inc(activity);
			int n = prevActivities.size();
			String prevActivity = prevActivities.get(--n);
			while (n > 0 && prevActivity != null && model.get(prevActivity, activity) == 0) {
				prevActivity = prevActivities.get(--n);
			}
			if (prevActivity != null) {
				correctedModel.inc(prevActivity, activity);
			}
		}
		return correctedModel;
	}

	private LogSkeletonCount correct(LogSkeletonCount model) {
		LogSkeletonCount correctedModel = new LogSkeletonCount();

		for (String fromActivity : model.getActivities()) {
			correctedModel.add(fromActivity, model.get(fromActivity));
			for (String toActivity : model.getActivities()) {
				if (model.get(fromActivity, toActivity) > 0 && model.get(toActivity, fromActivity) == 0) {
					correctedModel.add(fromActivity, toActivity, model.get(fromActivity, toActivity));
				}
			}
		}
		return correctedModel;
	}

	private LogSkeletonCount correct2(LogSkeletonCount model) {
		LogSkeletonCount correctedModel = new LogSkeletonCount();

		for (String fromActivity : model.getActivities()) {
			correctedModel.add(fromActivity, model.get(fromActivity));
			for (String toActivity : model.getActivities()) {
				int threshold = Math.min(model.get(fromActivity), model.get(toActivity)) / 5;
				if (model.get(fromActivity, toActivity) > threshold) {
					correctedModel.add(fromActivity, toActivity, model.get(fromActivity, toActivity));
				}
			}
		}
		return correctedModel;
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

	private Collection<LogSkeletonCount> createCounts(EventLogArray logs) {
		Collection<LogSkeletonCount> models = new ArrayList<LogSkeletonCount>();
		for (int i = 0; i < logs.getSize(); i++) {
			XLog log = logs.getLog(i);
			models.add(count(log));
		}
		return models;
	}

	private void addSameCounts(Collection<LogSkeletonCount> countModels, LogSkeleton constraintModel) {
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
		for (Set<String> sameCount : map.values()) {
			constraintModel.addSameCount(sameCount);
		}
	}

	private void createCausalDependencies(XLog log, LogSkeletonCount model, LogSkeleton constraintModel) {
		for (XTrace trace : log) {
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
				}
				String activity = postset.remove(0);
				constraintModel.addPrePost(activity, preset, postset);
				prevActivity = activity;
			}
		}
		constraintModel.cleanPrePost();
	}
}
