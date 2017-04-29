package org.processmining.logskeleton.algorithms;

import java.util.ArrayList;
import java.util.Collections;
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

public class PDC2017LogFilterAlgorithm {

	public XLog applyPrefix(XLog log) {
		Map<XTrace, List<String>> traceMap = new HashMap<XTrace, List<String>>();
		for (XTrace trace : log) {
			List<String> activities = new ArrayList<String>();
			for (XEvent event : trace) {
				activities.add(XConceptExtension.instance().extractName(event));
			}
			traceMap.put(trace, activities);
		}
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : log) {
			boolean ok = true;
			List<String> activities = traceMap.get(trace);
			for (List<String> otherActivities : traceMap.values()) {
				if (otherActivities.size() > activities.size()) {
					List<String> firstActivities = new ArrayList<String>();
					for (int i = 0; i < activities.size(); i++) {
						firstActivities.add(otherActivities.get(i));
					}
					if (firstActivities.containsAll(activities)) {
						ok = false;
					}
				}
			}
			if (ok) {
				filteredLog.add(trace);
			}
		}
		System.out.println("[PDC2017FilterAglorithm] Filtered log contains " + filteredLog.size() + " traces");
		return filteredLog;
	}
	
	public XLog applyEndActivities(XLog log) {
		Map<String, Integer> endCountMap = new HashMap<String, Integer>();
		for (XTrace trace : log) {
			String endActivity = XConceptExtension.instance().extractName(trace.get(trace.size() - 1));
			int count = 0;
			if (endCountMap.containsKey(endActivity)) {
				count = endCountMap.get(endActivity);
			}
			endCountMap.put(endActivity, count + 1);
		}
		List<Integer> endCountList = new ArrayList<Integer>(new HashSet<Integer>(endCountMap.values()));
		Collections.sort(endCountList);
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog();
		Set<String> endActivities = new HashSet<String>();
		while (filteredLog.size() * 100 < log.size() * 80) {
			int threshold = endCountList.remove(endCountList.size() - 1);
			for (XTrace trace : log) {
				String endActivity = XConceptExtension.instance().extractName(trace.get(trace.size() - 1));
				if (endCountMap.get(endActivity) == threshold) {
					if (endActivities.add(endActivity)) {
						System.out.println("[PDC2017FilterAglorithm] Added end activity: " + endActivity + " (" + threshold + ")");
					}
					filteredLog.add(trace);
				}
			}
		}
		System.out.println("[PDC2017FilterAglorithm] Filtered log contains " + filteredLog.size() + " traces, end activities are " + endActivities);
		return filteredLog;
	}
	
	public XLog apply(XLog log) {
		return applyPrefix(applyEndActivities(log));
	}
	
}
