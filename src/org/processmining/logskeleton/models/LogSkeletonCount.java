package org.processmining.logskeleton.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogSkeletonCount {

	public final static String STARTEVENT = "|>";
	public final static String ENDEVENT = "[]";
	
	private Map<String, Integer> activityCounts;
	private Map<List<String>, Integer> transitionCounts;
	
	public LogSkeletonCount() {
		activityCounts = new HashMap<String, Integer>();
		transitionCounts = new HashMap<List<String>, Integer>();
	}
	
	public boolean checkTransitionCounts(LogSkeletonCount model, Set<String> messages, String caseId) {
		for (List<String> transition : model.transitionCounts.keySet()) {
			if (!transitionCounts.keySet().contains(transition)) {
				messages.add("[LogSkeletonCount] Case " + caseId + ": Next fails for " + transition);
				return false;
			}
			if (transitionCounts.get(transition) < model.transitionCounts.get(transition)) {
				messages.add("[LogSkeletonCount] Case " + caseId + ": Next fails for " + transition);
				return false;
			}
		}
		return true;
	}
	public Integer get(String activity) {
		return activityCounts.containsKey(activity) ? activityCounts.get(activity) : 0;
	}
	
	public Integer get(String fromActivity, String toActivity) {
		List<String> transition = getTransition(fromActivity, toActivity);
		return transitionCounts.containsKey(transition) ? transitionCounts.get(transition) : 0;
	}
	
	public Collection<String> getTo(String fromActivity) {
		Collection<String> toActivities = new HashSet<String>();
		for (List<String> transition : transitionCounts.keySet()) {
			if (transition.get(0).equals(fromActivity)) {
				toActivities.add(transition.get(1));
			}
		}
		return toActivities;
	}
	
	public Collection<String> getFrom(String toActivity) {
		Collection<String> fromActivities = new HashSet<String>();
		for (List<String> transition : transitionCounts.keySet()) {
			if (transition.get(1).equals(toActivity)) {
				fromActivities.add(transition.get(0));
			}
		}
		return fromActivities;
	}
	
	
	public void add(String activity, Integer number) {
		if (activityCounts.containsKey(activity)) {
			activityCounts.put(activity, activityCounts.get(activity) + number);
		} else {
			activityCounts.put(activity, number);
		}
	}

	public void add(String fromActivity, String toActivity, Integer number) {
		List<String> transition = getTransition(fromActivity, toActivity);
		if (transitionCounts.containsKey(transition)) {
			transitionCounts.put(transition, transitionCounts.get(transition) + number);
		} else {
			transitionCounts.put(transition, number);
		}
	}

	public void inc(String activity) {
		add(activity, 1);
	}

	public void inc(String fromActivity, String toActivity) {
		add(fromActivity, toActivity, 1);
	}

	public Collection<String> getActivities() {
		List<String> ordered = new ArrayList<String>(activityCounts.keySet());
		Collections.sort(ordered);
		return ordered;
	}
	
	private List<String> getTransition(String fromActivity, String toActivity) {
		List<String> transition = new ArrayList<String>(2);
		transition.add(0, fromActivity);
		transition.add(1, toActivity);
		return transition;
	}
	
	public void print(String name) {
//		System.out.println("[PDC2017CountModel] Activity counts for " + name);
		for (String activity : activityCounts.keySet()) {
//			System.out.println("[LogSkeletonCount] " + activity + ": " + activityCounts.get(activity));
		}
//		System.out.println("[PC2017CountModel] Transitions counts for " + name);
		for (List<String> transition : transitionCounts.keySet()) {
//			System.out.println("[LogSkeletonCount] " + transition + ": " + transitionCounts.get(transition));
		}
	}
}
