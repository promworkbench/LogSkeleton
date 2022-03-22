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
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.models.TokenBasedReplayResult;
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

	private XLog repairedLog;
	private XTrace repairedTrace;

	public XLog apply(XLog log, XEventClassifier classifier, Petrinet net, Marking initialMarking,
			Marking finalMarking) {
		return apply(log, classifier, net, initialMarking, finalMarking, false);
	}

	public XLog apply(XLog log, XEventClassifier classifier, Petrinet net, Marking initialMarking, Marking finalMarking,
			boolean doRepair) {
		init(log, doRepair);
		init(net);
		for (XTrace trace : log) {
			classifiedLog.add(classify(trace, classifier, net, initialMarking, finalMarking));
		}
		return doRepair ? repairedLog : classifiedLog;
	}

	private void init(XLog log, boolean doRepair) {
		classifiedLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		repairedLog = doRepair ? XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes()) : null;
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
			if (transition.isInvisible() && transition.getLabel().startsWith(ConverterAlgorithm.PREFIX_START)) {
				if (inputPlaces.get(transition).size() == 1 && outputPlaces.get(transition).size() == 1) {
					placeMap.put(outputPlaces.get(transition).iterator().next(),
							inputPlaces.get(transition).iterator().next());
				} else if (inputPlaces.get(transition).size() == 0 && outputPlaces.get(transition).size() == 1) {
					placeMap.put(outputPlaces.get(transition).iterator().next(), null);
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
		if (repairedLog != null) {
			repairedTrace = XFactoryRegistry.instance().currentDefault().createTrace(trace.getAttributes());
		}
		List<String> activities = new ArrayList<String>();
		activities.add(LogSkeletonCount.STARTEVENT);
		for (XEvent event : trace) {
			activities.add(classifier.getClassIdentity(event));
			classifiedTrace.add(event);
		}
		activities.add(LogSkeletonCount.ENDEVENT);
		TokenBasedReplayResult replayResult = classify(activities, net, initialMarking, finalMarking);
		boolean isPos = replayResult.hasPerfectFitness();
		double hasFit = replayResult.getFitness();
		System.out.println("[PNClassifierAlgorithm] " + isPos + " " + hasFit + " " + activities);
		XAttributeBoolean isPosAttribute = XFactoryRegistry.instance().currentDefault()
				.createAttributeBoolean("pdc:isPos", isPos, null);
		classifiedTrace.getAttributes().put("pdc:isPos", isPosAttribute);
		XAttributeContinuous hasFitAttribute = XFactoryRegistry.instance().currentDefault()
				.createAttributeContinuous("pdc:hasFit", hasFit, null);
		classifiedTrace.getAttributes().put("pdc:hasFit", hasFitAttribute);
		if (repairedLog != null && isPos) {
			repairedLog.add(repairedTrace);
		}
		return classifiedTrace;
	}

	private TokenBasedReplayResult classify(List<String> activities, Petrinet net, Marking initialMarking,
			Marking finalMarking) {
		//		System.out.println("[PNClassifierAlgorithm] Replaying trace " + activities);
		Marking currentMarking = new Marking(initialMarking);
		TokenBasedReplayResult replayResult = new TokenBasedReplayResult();
		replayResult.addProduced(initialMarking.baseSet().size());
		for (String activity : activities) {
			Transition transition = transitionMap.get(activity);
			//						System.out.println("[PNClassifierAlgorithm] " + activity);
			//						System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
			if (!inputPlaces.containsKey(transition)) {
				System.out.println("[PNClassifierAlgorithm] Unknown transition: " + transition.getLabel());
				/*
				 * Unknown transition. Assume one input and one output.
				 */
				replayResult.addMissing(1);
				replayResult.addConsumed(1);
				replayResult.addRemaining(1);
				replayResult.addProduced(1);
				continue;
				//				return false;
			}
			for (Place place : inputPlaces.get(transition)) {
				/*
				 * Need to consume a token from this input place.
				 */
				replayResult.addConsumed(1);
				if (currentMarking.contains(place)) {
					/*
					 * Token is there. Consume it now.
					 */
					currentMarking.remove(place);
				} else if (placeMap.containsKey(place)) {
					if (placeMap.get(place) == null) {
						/*
						 * Place has a token generator. Use it to produce the
						 * token.
						 */
						replayResult.addProduced(1);
						if (repairedLog != null) {
							XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
							XConceptExtension.instance().assignName(event, "->" + place.getLabel());
							repairedTrace.add(event);
						}
					} else if (currentMarking.contains(placeMap.get(place))) {
						/*
						 * Place can get a token on one step from another place.
						 * Get it.
						 */
						replayResult.addConsumed(1);
						replayResult.addProduced(1);
						if (repairedLog != null) {
							XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
							XConceptExtension.instance().assignName(event,
									placeMap.get(place).getLabel() + "->" + place.getLabel());
							repairedTrace.add(event);
						}
						currentMarking.remove(placeMap.get(place));
					} else if (placeMap.containsKey(placeMap.get(place))) {
						/*
						 * Place can get a token in two steps from another
						 * place. Get it.
						 */
						if (placeMap.get(placeMap.get(place)) == null) {
							/*
							 * First step is a token generator.
							 */
							replayResult.addConsumed(1);
							replayResult.addProduced(2);
							if (repairedLog != null) {
								XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
								XConceptExtension.instance().assignName(event, "->" + placeMap.get(place).getLabel());
								repairedTrace.add(event);
								event = XFactoryRegistry.instance().currentDefault().createEvent();
								XConceptExtension.instance().assignName(event,
										placeMap.get(place).getLabel() + "->" + place.getLabel());
								repairedTrace.add(event);
							}
						} else if (currentMarking.contains(placeMap.get(placeMap.get(place)))) {
							/*
							 * First step is not a token generator.
							 */
							replayResult.addConsumed(2);
							replayResult.addProduced(2);
							if (repairedLog != null) {
								XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
								XConceptExtension.instance().assignName(event,
										placeMap.get(placeMap.get(place)).getLabel() + "->"
												+ placeMap.get(place).getLabel());
								repairedTrace.add(event);
								event = XFactoryRegistry.instance().currentDefault().createEvent();
								XConceptExtension.instance().assignName(event,
										placeMap.get(place).getLabel() + "->" + place.getLabel());
								repairedTrace.add(event);
							}
							currentMarking.remove(placeMap.get(placeMap.get(place)));
						}
					} else {
						replayResult.addMissing(1);
						//						return false;
					}
				} else {
					replayResult.addMissing(1);
					//					return false;
				}
			}
			//						System.out.println("[PNClassifierAlgorithm] Replayed activity " + activity);
			if (repairedLog != null) {
				XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
				XConceptExtension.instance().assignName(event, activity);
				repairedTrace.add(event);
			}
			for (Place place : outputPlaces.get(transition)) {
				currentMarking.add(place);
			}
		}

		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		for (Place place : finalMarking.baseSet()) {
			while (finalMarking.occurrences(place) > currentMarking.occurrences(place)) {
				/*
				 * We're some tokens short for this place in the final marking.
				 */
				if (placeMap.containsKey(place)) {
					/*
					 * Tokens may be coming for this place.
					 */
					if (placeMap.get(place) == null) {
						/*
						 * Place has a token generator. Generate sufficiently
						 * many tokens for this place.
						 */
						replayResult.addProduced(finalMarking.occurrences(place) - currentMarking.occurrences(place));
						if (repairedLog != null) {
							for (int i = currentMarking.occurrences(place); i < finalMarking.occurrences(place); i++) {
								XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
								XConceptExtension.instance().assignName(event,
										placeMap.get(place).getLabel() + "->" + place.getLabel());
								repairedTrace.add(event);
							}
						}
						currentMarking.add(place, finalMarking.occurrences(place) - currentMarking.occurrences(place));
					} else if (currentMarking.contains(placeMap.get(place))) {
						replayResult.addConsumed(1);
						replayResult.addProduced(1);
						/*
						 * Predecessor place contains tokens. Move one to this
						 * place.
						 */
						if (repairedLog != null) {
							XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
							XConceptExtension.instance().assignName(event,
									placeMap.get(place).getLabel() + "->" + place.getLabel());
							repairedTrace.add(event);
						}
						currentMarking.remove(placeMap.get(place));
						currentMarking.add(place);
					} else if (placeMap.containsKey(placeMap.get(place))) {
						/*
						 * Tokens may be coming for the predecessor place.
						 */
						if (placeMap.get(placeMap.get(place)) == null) {
							/*
							 * Predecessor place has a token generator. Generate
							 * sufficiently many tokens for this place.
							 */
							replayResult
									.addConsumed(finalMarking.occurrences(place) - currentMarking.occurrences(place));
							replayResult.addProduced(
									2 * (finalMarking.occurrences(place) - currentMarking.occurrences(place)));
							if (repairedLog != null) {
								for (int i = currentMarking.occurrences(place); i < finalMarking
										.occurrences(place); i++) {
									XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
									XConceptExtension.instance().assignName(event,
											placeMap.get(placeMap.get(place)).getLabel() + "->"
													+ placeMap.get(place).getLabel());
									repairedTrace.add(event);
									event = XFactoryRegistry.instance().currentDefault().createEvent();
									XConceptExtension.instance().assignName(event,
											placeMap.get(place).getLabel() + "->" + place.getLabel());
									repairedTrace.add(event);
								}
							}
							currentMarking.add(place,
									finalMarking.occurrences(place) - currentMarking.occurrences(place));
						} else if (currentMarking.contains(placeMap.get(placeMap.get(place)))) {
							/*
							 * Predecessor of predecessor place contains tokens.
							 * Move one to this place.
							 */
							replayResult.addConsumed(2);
							replayResult.addProduced(2);
							if (repairedLog != null) {
								XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
								XConceptExtension.instance().assignName(event,
										placeMap.get(placeMap.get(place)).getLabel() + "->"
												+ placeMap.get(place).getLabel());
								repairedTrace.add(event);
								event = XFactoryRegistry.instance().currentDefault().createEvent();
								XConceptExtension.instance().assignName(event,
										placeMap.get(place).getLabel() + "->" + place.getLabel());
								repairedTrace.add(event);
							}
							currentMarking.remove(placeMap.get(placeMap.get(place)));
							currentMarking.add(place);
						} else {
							replayResult.addMissing(1);
							replayResult.addConsumed(1);
							break;
						}
					} else {
						replayResult.addMissing(1);
						replayResult.addConsumed(1);
						break;
					}
				} else {
					replayResult.addMissing(1);
					replayResult.addConsumed(1);
					break;
				}
			}
		}
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_INTERVAL_T1, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_EQUIVALENCE_T1, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_AFTER_T2, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_BEFORE_T1, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_BEFORE_T2, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_NEVER_T1, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_NEVER_T2, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_EXCLUSIVE_T1, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_EXCLUSIVE_T2, replayResult);
		//		System.out.println("[PNClassifierAlgorithm] Current marking: " + currentMarking);
		currentMarking = fireAll(net, currentMarking, ConverterAlgorithm.PREFIX_EXCLUSIVE_T3, replayResult);

		System.out.println("[PNClassifierAlgorithm] Adapted current marking: " + currentMarking);
		System.out.println("[PNClassifierAlgorithm] Final marking: " + finalMarking);
		for (Place place : currentMarking.baseSet()) {
			while (currentMarking.occurrences(place) > finalMarking.occurrences(place)) {
				replayResult.addRemaining(1);
				currentMarking.remove(place);
			}
			while (currentMarking.occurrences(place) < finalMarking.occurrences(place)) {
				replayResult.addMissing(1);
				currentMarking.add(place);
			}
		}
		for (Place place : finalMarking.baseSet()) {
			replayResult.addConsumed(finalMarking.occurrences(place));
		}
		return replayResult; //currentMarking.equals(finalMarking);
	}

	private Marking fireAll(Petrinet net, Marking currentMarking, String prefix, TokenBasedReplayResult replayResult) {
		for (Transition transition : net.getTransitions()) {
			if (transition.getLabel().startsWith(prefix)) {
				boolean enabled = !inputPlaces.get(transition).isEmpty();
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
						replayResult.addConsumed(inputPlaces.get(transition).size());
						replayResult.addProduced(outputPlaces.get(transition).size());
						//												System.out.println("[PNClassifierAlgorithm] Fired transition " + transition.getLabel());
						if (repairedLog != null) {
							String label = transition.getLabel();
							Place inputPlace = (inputPlaces.get(transition).size() == 1)
									? inputPlaces.get(transition).iterator().next()
									: null;
							Place outputPlace = (outputPlaces.get(transition).size() == 1)
									? outputPlaces.get(transition).iterator().next()
									: null;
							if (inputPlace != null && outputPlace != null) {
								label = inputPlace.getLabel() + "->" + outputPlace.getLabel();
							} else if (inputPlace != null && outputPlaces.get(transition).size() == 0) {
								label = inputPlace.getLabel() + "->";
							}
							Set<Transition> inputTransitions = new HashSet<Transition>();
							for (Transition candidateInputTransition : net.getTransitions()) {
								if (outputPlaces.get(candidateInputTransition).contains(inputPlace)) {
									inputTransitions.add(candidateInputTransition);
								}
							}
							Set<String> inputLabels = new HashSet<String>();
							for (Transition inputTransition : inputTransitions) {
								inputLabels.add(inputTransition.getLabel());
							}
							int n = repairedTrace.size();
							for (int i = repairedTrace.size() - 1; i >= 0; i--) {
								if (inputLabels
										.contains(XConceptExtension.instance().extractName(repairedTrace.get(i)))) {
									n = i + 1;
									break;
								}
							}
							XEvent event = XFactoryRegistry.instance().currentDefault().createEvent();
							XConceptExtension.instance().assignName(event, label);
							repairedTrace.add(n, event);
						}
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
