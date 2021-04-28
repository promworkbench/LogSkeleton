package org.processmining.logskeleton.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class PNClassifierAlgorithm {

	private Map<Place, Place> placeMap;
	private Map<String, Transition> transitionMap;
	private Map<Transition, Collection<Place>> inputPlaces;
	private Map<Transition, Collection<Place>> outputPlaces;
	private XLog classifiedLog;

	public XLog apply(XLog log, XEventClassifier classifier, Petrinet net, Marking initialMarking,
			Marking finalMarking) {
		init(log);
		init(net);
		for (XTrace trace : log) {
			classifiedLog.add(classify(trace, classifier, net, initialMarking, finalMarking));
		}
		return classifiedLog;
	}

	private void init(XLog log) {
		classifiedLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
	}

	private void init(Petrinet net) {
		inputPlaces = new HashMap<Transition, Collection<Place>>();
		outputPlaces = new HashMap<Transition, Collection<Place>>();
		for (Transition transition : net.getTransitions()) {
			inputPlaces.put(transition, new ArrayList<Place>());
			outputPlaces.put(transition, new ArrayList<Place>());
		}
		for (PetrinetEdge<?, ?> edge : net.getEdges()) {
			if (edge.getSource() instanceof Transition) {
				outputPlaces.get(edge.getSource()).add((Place) edge.getTarget());
			} else {
				inputPlaces.get(edge.getTarget()).add((Place) edge.getSource());
			}
		}
		placeMap = new HashMap<Place, Place>();
		transitionMap = new HashMap<String, Transition>();
		for (Transition transition : net.getTransitions()) {
			if (transition.isInvisible() && transition.getLabel().startsWith("{")) {
				if (inputPlaces.get(transition).size() == 1 && outputPlaces.get(transition).size() == 1) {
					placeMap.put(outputPlaces.get(transition).iterator().next(),
							inputPlaces.get(transition).iterator().next());
				}
			}
			if (!transition.isInvisible() || transition.getLabel().equals(LogSkeletonCount.STARTEVENT)
					|| transition.getLabel().equals(LogSkeletonCount.ENDEVENT)) {
				transitionMap.put(transition.getLabel(), transition);
			}
		}
	}

	private XTrace classify(XTrace trace, XEventClassifier classifier, Petrinet net, Marking initialMarking,
			Marking finalMarking) {
		XTrace classifiedTrace = XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
		List<String> activities = new ArrayList<String>();
		activities.add(LogSkeletonCount.STARTEVENT);
		for (XEvent event : trace) {
			activities.add(classifier.getClassIdentity(event));
			classifiedTrace.add(event);
		}
		activities.add(LogSkeletonCount.ENDEVENT);
		boolean isPos = classify(activities, net, initialMarking, finalMarking);
		System.out.println("[PNClassifierAlgorithm] " + isPos + " " + activities);
		XAttributeBoolean isPosAttribute = XFactoryRegistry.instance().currentDefault()
				.createAttributeBoolean("pdc:isPos", isPos, null);
		classifiedTrace.getAttributes().put("pdc:isPos", isPosAttribute);
		return classifiedTrace;
	}

	private boolean classify(List<String> activities, Petrinet net, Marking initialMarking, Marking finalMarking) {
//		System.out.println("[PNClassifierAlgorithm] Replaying trace " + activities);
		Marking currentMarking = new Marking(initialMarking);
		for (String activity : activities) {
			Transition transition = transitionMap.get(activity);
			for (Place place : inputPlaces.get(transition)) {
				if (currentMarking.contains(place)) {
					currentMarking.remove(place);
				} else if (placeMap.containsKey(place)) {
					if (currentMarking.contains(placeMap.get(place))) {
						currentMarking.remove(placeMap.get(place));
					} else if (placeMap.containsKey(placeMap.get(place))) {
						currentMarking.remove(placeMap.get(placeMap.get(place)));
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
//			System.out.println("[PNClassifierAlgorithm] Replayed activity " + activity);
			for (Place place : outputPlaces.get(transition)) {
				currentMarking.add(place);
			}
		}
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{ti}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{te}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{tb}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{tn}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{un}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{tx}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{ux}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, "{vx}");
//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
//		System.out.println("[PNClassifierAlgorithm] Final marking: " + finalMarking);
		return currentMarking.equals(finalMarking);
	}

	private Marking fireAll(Petrinet net, Marking currentMarking, String prefix) {
		for (Transition transition : net.getTransitions()) {
			if (transition.getLabel().startsWith(prefix)) {
				boolean enabled = true;
				while (enabled) {
					Marking tmpMarking = new Marking(currentMarking);
					for (Place place : inputPlaces.get(transition)) {
						if (tmpMarking.contains(place)) {
							tmpMarking.remove(place);
						} else {
							enabled = false;
						}
					}
					if (enabled) {
//						System.out.println("[PNClassifierAlgorithm] Fired transition " + transition.getLabel());
						currentMarking = tmpMarking;
						for (Place place : outputPlaces.get(transition)) {
							currentMarking.add(place);
						}
					}
				}
			}
		}
		return currentMarking;
	}
}
