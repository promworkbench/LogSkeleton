package org.processmining.logskeleton.algorithms;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.parameters.SplitterParameters;

public class SplitterAlgorithm {

	public XLog apply(XLog log, SplitterParameters parameters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		for (XTrace trace : log) {
			XTrace filteredTrace = XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
			int milestone = 0;
			for (XEvent event : trace) {
				String activity = XConceptExtension.instance().extractName(event);
				if (activity.equals(parameters.getDuplicateActivity())) {
					XEvent filteredEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					XConceptExtension.instance().assignName(filteredEvent, activity + "." + milestone);
					filteredTrace.add(filteredEvent);
				} else {
					filteredTrace.add(event);
				}
				if (parameters.getMilestoneActivities().contains(activity)) {
					if (milestone == 0) {
						milestone++;
					}
				}
			}
			filteredLog.add(filteredTrace);
		}
//		System.out.println("[SplitterAlgorithm] Split log contains " + filteredLog.size() + " traces");
		return filteredLog;
	}

	public XLog apply7B(XLog log) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		for (XTrace trace : log) {
			XTrace filteredTrace = XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
			for (int i = 0; i < trace.size(); i++) {
				if (i == trace.size() - 1 && XConceptExtension.instance().extractName(trace.get(i)).equals("b")) {
					XEvent filteredEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					XConceptExtension.instance().assignName(filteredEvent, "b.1");
					filteredTrace.add(filteredEvent);
				} else if (i == trace.size() - 2 && XConceptExtension.instance().extractName(trace.get(i)).equals("b")
						&& XConceptExtension.instance().extractName(trace.get(i + 1)).equals("s")) {
					XEvent filteredEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					XConceptExtension.instance().assignName(filteredEvent, "b.1");
					filteredTrace.add(filteredEvent);
				} else if (XConceptExtension.instance().extractName(trace.get(i)).equals("b")) {
					XEvent filteredEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					XConceptExtension.instance().assignName(filteredEvent, "b.0");
					filteredTrace.add(filteredEvent);
				} else {
					filteredTrace.add(trace.get(i));
				}
			}
			filteredLog.add(filteredTrace);
		}
//		System.out.println("[SplitterAlgorithm] Split log contains " + filteredLog.size() + " traces");
		return filteredLog;
	}

}
