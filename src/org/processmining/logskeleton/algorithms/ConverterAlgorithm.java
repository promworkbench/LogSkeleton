package org.processmining.logskeleton.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.configurations.ConverterConfiguration;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.models.LogSkeletonEdge;
import org.processmining.logskeleton.models.LogSkeletonEdgeType;
import org.processmining.logskeleton.models.LogSkeletonGraph;
import org.processmining.logskeleton.models.LogSkeletonNode;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;

public class ConverterAlgorithm {

	private Petrinet net;
	private Map<LogSkeletonNode, Transition> transitions;
	private Transition startTransition;
	private Transition endTransition;
	private Marking startMarking;
	private Marking endMarking;

	static public String PREFIX_START = "{";
	static public String PREFIX_END = "}";

	static public String PREFIX_PROCESS_P1 = PREFIX_START + "i" + PREFIX_END;
	static public String PREFIX_PROCESS_P2 = PREFIX_START + "o" + PREFIX_END;

	static public String PREFIX_INTERVAL_P1 = PREFIX_START + "pi" + PREFIX_END;
	static public String PREFIX_INTERVAL_P2 = PREFIX_START + "qi" + PREFIX_END;
	static public String PREFIX_INTERVAL_P3 = PREFIX_START + "ri" + PREFIX_END;

	static public String PREFIX_EQUIVALENCE_P1 = PREFIX_START + "pe" + PREFIX_END;

	static public String PREFIX_AFTER_P1 = PREFIX_START + "pa" + PREFIX_END;
	static public String PREFIX_AFTER_P2 = PREFIX_START + "qa" + PREFIX_END;
	static public String PREFIX_AFTER_P3 = PREFIX_START + "ra" + PREFIX_END;

	static public String PREFIX_BEFORE_P1 = PREFIX_START + "pb" + PREFIX_END;
	static public String PREFIX_BEFORE_P2 = PREFIX_START + "qb" + PREFIX_END;
	static public String PREFIX_BEFORE_P3 = PREFIX_START + "rb" + PREFIX_END;

	static public String PREFIX_NEVER_P1 = PREFIX_START + "pn" + PREFIX_END;
	static public String PREFIX_NEVER_P2 = PREFIX_START + "qn" + PREFIX_END;
	static public String PREFIX_NEVER_P3 = PREFIX_START + "rn" + PREFIX_END;

	static public String PREFIX_EXCLUSIVE_P1 = PREFIX_START + "px" + PREFIX_END;
	static public String PREFIX_EXCLUSIVE_P2 = PREFIX_START + "qx" + PREFIX_END;
	static public String PREFIX_EXCLUSIVE_P3 = PREFIX_START + "rx" + PREFIX_END;

	static public String PREFIX_INTERVAL_T1 = PREFIX_START + "ti" + PREFIX_END;
	static public String PREFIX_INTERVAL_T2 = PREFIX_START + "ui" + PREFIX_END;

	static public String PREFIX_EQUIVALENCE_T1 = PREFIX_START + "te" + PREFIX_END;

	static public String PREFIX_AFTER_T1 = PREFIX_START + "ta" + PREFIX_END;
	static public String PREFIX_AFTER_T2 = PREFIX_START + "ua" + PREFIX_END;

	static public String PREFIX_BEFORE_T1 = PREFIX_START + "tb" + PREFIX_END;
	static public String PREFIX_BEFORE_T2 = PREFIX_START + "ub" + PREFIX_END;

	static public String PREFIX_NEVER_T1 = PREFIX_START + "tn" + PREFIX_END;
	static public String PREFIX_NEVER_T2 = PREFIX_START + "un" + PREFIX_END;

	static public String PREFIX_EXCLUSIVE_T1 = PREFIX_START + "tx" + PREFIX_END;
	static public String PREFIX_EXCLUSIVE_T2 = PREFIX_START + "ux" + PREFIX_END;
	static public String PREFIX_EXCLUSIVE_T3 = PREFIX_START + "vx" + PREFIX_END;

	public Petrinet apply(PluginContext context, LogSkeletonGraph graph, ConverterConfiguration configuration) {
		// Initialize.
		init(graph, configuration);
		initMerge(graph, configuration);

		// Add all fragments for the seelcted constraints.
		addInterval(graph, configuration);
		addEquivalence(graph, configuration);
		addAlwaysAfter(graph, configuration);
		addAlwaysBefore(graph, configuration);
		addNever(graph, configuration);
		addExclusive(graph, configuration);

		// Connect the net with the start (initial) and end (final) marking.
		connect(context);

		return net;
	}

	private void init(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		net = PetrinetFactory.newPetrinet("Petri net converted from log skeleton " + graph.getTitle());

		transitions = new HashMap<LogSkeletonNode, Transition>();
		startMarking = new Marking();
		endMarking = new Marking();
		if (!configuration.isMerge()) {
			Place startPlace = net.addPlace(PREFIX_PROCESS_P1);
			Place endPlace = net.addPlace(PREFIX_PROCESS_P2);
			/*
			 * We're using "{" as first character of a silent transition as this character
			 * follows all letters in the ASCII table. As a result, when sorted, the added
			 * silent transitions will come last.
			 */
			startTransition = net.addTransition(LogSkeletonCount.STARTEVENT);
			startTransition.setInvisible(true);
			endTransition = net.addTransition(LogSkeletonCount.ENDEVENT);
			endTransition.setInvisible(true);
			net.addArc(startPlace, startTransition);
			net.addArc(endTransition, endPlace);

			startMarking.add(startPlace);
			endMarking.add(endPlace);
		}
	}

	private void connect(PluginContext context) {
		context.getProvidedObjectManager().createProvidedObject("Initial marking for " + net.getLabel(), startMarking,
				Marking.class, context);
		context.addConnection(new InitialMarkingConnection(net, startMarking));
		context.getProvidedObjectManager().createProvidedObject("Final marking for " + net.getLabel(), endMarking,
				Marking.class, context);
		context.addConnection(new FinalMarkingConnection(net, endMarking));
	}

	private void initMerge(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isMerge()) {
			// Add a transition for every node.
			for (LogSkeletonNode node : graph.getNodes()) {
				Transition transition = net.addTransition(node.getLabel());
				if (node.getLabel().equals(LogSkeletonCount.STARTEVENT)) {
					// Artificial start activity, make transition silent.
					startTransition = transition;
					transition.setInvisible(true);
					Place place = net.addPlace(PREFIX_PROCESS_P1);
					startMarking.add(place);
					net.addArc(place, transition);
				} else if (node.getLabel().equals(LogSkeletonCount.ENDEVENT)) {
					// Artificial end activity, make transition silent.
					endTransition = transition;
					transition.setInvisible(true);
					Place place = net.addPlace(PREFIX_PROCESS_P2);
					endMarking.add(place);
					net.addArc(transition, place);
				}
				transitions.put(node, transition);
			}
		}
	}

	private void addInterval(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isInterval()) {
			// Intervals
			for (LogSkeletonNode node : graph.getNodes()) {
				/*
				 * Activity A (where A equals node.getLabel()).
				 */
				if (node.getLabel().equals(LogSkeletonCount.STARTEVENT)
						|| node.getLabel().equals(LogSkeletonCount.ENDEVENT)) {
					continue;
				}
				if (configuration.isEquivalence() && !node.getLabel().equals(node.getLabelRepresentative())) {
					continue;
				}
				Transition aX = configuration.isMerge() ? transitions.get(node) : net.addTransition(node.getLabel());
				Place piX = net.addPlace(PREFIX_INTERVAL_P1 + node.getLabel());
				Place qiX = net.addPlace(PREFIX_INTERVAL_P2 + node.getLabel());
				net.addArc(piX, aX);
				net.addArc(aX, qiX);
				/*
				 * In the end, we want to have high(A) tokens in the qiA place.
				 */
				if (configuration.isMarking()) {
					endMarking.add(qiX, node.getHigh());
				} else {
					net.addArc(qiX, endTransition, node.getHigh());
				}
				if (node.getLow() == node.getHigh()) {
					/*
					 * Activity A always occurs high times. Put high tokens in the piA place.
					 */
					if (configuration.isMarking()) {
						startMarking.add(piX, node.getHigh());
					} else {
						net.addArc(startTransition, piX, node.getHigh());
					}
				} else if (node.getLow() == 0) {
					/*
					 * Activity A may not occur at all. Put high tokens in the piA place.
					 */
					if (configuration.isMarking()) {
						startMarking.add(piX, node.getHigh());
					} else {
						net.addArc(startTransition, piX, node.getHigh());
					}
					/*
					 * Transition tA skips the activity A.
					 */
					Transition tX = net.addTransition(PREFIX_INTERVAL_T1 + node.getLabel());
					tX.setInvisible(true);
					net.addArc(piX, tX);
					net.addArc(tX, qiX);
				} else {
					/*
					 * Activity A needs to occur at least low times, but at most high times.
					 */
					Place riX = net.addPlace(PREFIX_INTERVAL_P3 + node.getLabel());
					/*
					 * Transition t1A skips the activity A.
					 */
					Transition tiX = net.addTransition(PREFIX_INTERVAL_T1 + node.getLabel());
					tiX.setInvisible(true);
					/*
					 * Transition uiA avoids skipping the activity A.
					 */
					Transition uiX = net.addTransition(PREFIX_INTERVAL_T2 + node.getLabel());
					uiX.setInvisible(true);
					/*
					 * Add low tokens to the p2 place. This many times, the activity may be skipped.
					 */
					if (configuration.isMarking()) {
						startMarking.add(piX, node.getLow());
					} else {
						net.addArc(startTransition, piX, node.getLow());
					}
					/*
					 * Add the remaining high-low tokens in the p1 place. This many times, the
					 * activity may not be skipped.
					 */
					if (configuration.isMarking()) {
						startMarking.add(riX, node.getHigh() - node.getLow());
					} else {
						net.addArc(startTransition, riX, node.getHigh() - node.getLow());
					}
					/*
					 * Connect everything.
					 */
					net.addArc(riX, uiX);
					net.addArc(uiX, piX);
					net.addArc(riX, tiX);
					net.addArc(tiX, qiX);
				}
			}
		}
	}

	private void addEquivalence(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isEquivalence()) {
			// Equivalence
			for (LogSkeletonNode node : graph.getNodes()) {
				if (node.getLabelRepresentative().equals(node.getLabel())) {
					/*
					 * Found a representative. Check this equivalence class. First, get all required
					 * nodes.
					 */
					Set<LogSkeletonNode> requiredNodes = new HashSet<LogSkeletonNode>();
					for (LogSkeletonNode candidateRequiredNode : graph.getNodes()) {
						if (candidateRequiredNode.getLabelRepresentative().equals(node.getLabel())) {
							/*
							 * Candidate node is part of this equivalence class. OK.
							 */
							boolean isRequired = true;
							if (configuration.isOptimizeEquivalence() && configuration.isOptimizeAlwaysEquivalence()
									&& configuration.isAlwaysAfter() && candidateRequiredNode.getHigh() <= 1) {
								/*
								 * Check if there is an equivalent preceding node that has always after with
								 * this node. If so, this edge will take care of the equivalence check with that
								 * node.
								 */
								for (LogSkeletonEdge edge : candidateRequiredNode.getIncoming().values()) {
									if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS && node.getHigh() <= 1
											&& edge.getTailNode().getHigh() <= 1
											&& edge.getTailNode().getLabelRepresentative().equals(node.getLabel())) {
										/*
										 * Not needed as candidate. The preceding node will take care of the equivalence
										 * of this node.
										 */
										isRequired = false;
										break;
									}
								}
							}
							if (configuration.isOptimizeEquivalence() && configuration.isOptimizeAlwaysEquivalence()
									&& configuration.isAlwaysBefore() && candidateRequiredNode.getHigh() <= 1) {
								/*
								 * Check if there is an equivalent preceding node that has always before with
								 * this node. If so, this edge will take care of the equivalence check with that
								 * node.
								 */
								for (LogSkeletonEdge edge : candidateRequiredNode.getIncoming().values()) {
									if (edge.getHeadType() == LogSkeletonEdgeType.ALWAYS && node.getHigh() <= 1
											&& edge.getTailNode().getHigh() <= 1
											&& edge.getTailNode().getLabelRepresentative().equals(node.getLabel())) {
										/*
										 * Not needed as candidate. The preceding node will take care of the equivalence
										 * of this node.
										 */
										isRequired = false;
										break;
									}
								}
							}
							if (configuration.isOptimizeEquivalence() && configuration.isOptimizeNeverEquivalence()
									&& configuration.isNever() && candidateRequiredNode.getHigh() <= 1) {
								/*
								 * Check if there is an equivalent preceding node that has never after/before
								 * with this node. If so, this edge will take care of the equivalence check with
								 * that node.
								 */
								for (LogSkeletonEdge edge : candidateRequiredNode.getIncoming().values()) {
									if ((edge.getTailType() == LogSkeletonEdgeType.NEVER
											|| edge.getHeadType() == LogSkeletonEdgeType.NEVER) && node.getHigh() <= 1
											&& edge.getTailNode().getHigh() <= 1
											&& edge.getTailNode().getLabelRepresentative().equals(node.getLabel())) {
										/*
										 * Not needed as candidate. The preceding node will take care of the equivalence
										 * of this node.
										 */
										isRequired = false;
										break;
									}
								}
							}
							if (isRequired) {
								requiredNodes.add(candidateRequiredNode);
							}
						}
					}
					if (requiredNodes.size() > 1) {
						System.out.println("[ConverterAlgorithm] Creating fragment for " + requiredNodes);
						Transition teA = net.addTransition(PREFIX_EQUIVALENCE_T1 + node.getLabel());
						teA.setInvisible(true);
						for (LogSkeletonNode requiredNode : requiredNodes) {
							Transition aX = configuration.isMerge() ? transitions.get(requiredNode)
									: net.addTransition(requiredNode.getLabel());
							Place peX = net.addPlace(PREFIX_EQUIVALENCE_P1 + requiredNode.getLabel());
							net.addArc(aX, peX);
							net.addArc(peX, teA);
						}
					} else {
						System.out.println("[ConverterAlgorithm] Equivalence class for " + node.getLabel()
								+ " has only a single required node.");
					}
				}
			}
		}
	}

	private void addAlwaysAfter(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isAlwaysAfter()) {
			for (LogSkeletonEdge edge : graph.getEdges().values()) {
				if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS) {
					// Head (B) always after tail (A)
					Transition aA = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition aB = configuration.isMerge() ? transitions.get(edge.getHeadNode())
							: net.addTransition(edge.getHeadNode().getLabel());
					if (configuration.isOptimizeAlwaysNever() && configuration.isOptimizeNeverEquivalence()
							&& configuration.isNever() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getHeadType() == LogSkeletonEdgeType.NEVER
							&& edge.getTailNode().getLabelRepresentative()
									.equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Avoid duplication: addNever() will take care of this.
						 */
					} else if (configuration.isOptimizeAlwaysEquivalence() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getTailNode().getLabelRepresentative()
									.equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Both A and B occur at most once, and always occur equally often. The paAB
						 * place between them will do.
						 */
						Place paAB = net.addPlace(PREFIX_AFTER_P1 + edge.toString());
						net.addArc(aA, paAB);
						net.addArc(paAB, aB);
					} else if (configuration.isOptimizeAlwaysElementary() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1) {
						Place paAB = net.addPlace(PREFIX_AFTER_P1 + edge.toString());
						Place qaAB = net.addPlace(PREFIX_AFTER_P2 + edge.toString());
						Place raAB = net.addPlace(PREFIX_AFTER_P3 + edge.toString());
						if (configuration.isMarking()) {
							startMarking.add(paAB);
							endMarking.add(raAB);
						} else {
							net.addArc(startTransition, paAB);
							net.addArc(raAB, endTransition);
						}
						Transition taAB = net.addTransition(PREFIX_AFTER_T1 + edge.toString());
						taAB.setInvisible(true);
						Transition uaAB = net.addTransition(PREFIX_AFTER_T2 + edge.toString());
						uaAB.setInvisible(true);
						net.addArc(paAB, aA);
						net.addArc(paAB, taAB);
						net.addArc(aA, qaAB);
						net.addArc(taAB, qaAB);
						net.addArc(qaAB, aB);
						net.addArc(aB, raAB);
						net.addArc(paAB, uaAB);
						net.addArc(uaAB, raAB);
					} else {
						/*
						 * Place qaAB models that the constraint is not satisfied: We need to do B.
						 */
						Place qaAB = net.addPlace(PREFIX_AFTER_P2 + edge.toString());
						/*
						 * Place paAB models that the constraint is satisfied.
						 */
						Place paAB = net.addPlace(PREFIX_AFTER_P1 + edge.toString());
						/*
						 * Initially, the constraint is satisfied.
						 */
						if (configuration.isMarking()) {
							startMarking.add(paAB);
							endMarking.add(paAB);
						} else {
							net.addArc(startTransition, paAB);
							net.addArc(paAB, endTransition);
						}
						/*
						 * Transition taAB models two possibilities: 1. Either we need to do B, which
						 * will violate the constraint. 2. Or we need to do A, which will not violate
						 * the constraint.
						 */
						Transition taAB = net.addTransition(PREFIX_AFTER_T1 + edge.toString());
						taAB.setInvisible(true);
						/*
						 * Connect everything.
						 */
						net.addArc(paAB, taAB);
						net.addArc(taAB, qaAB);
						/*
						 * Firing B restores the constraint.
						 */
						net.addArc(qaAB, aB);
						net.addArc(aB, paAB);
						/*
						 * Firing A (temporarily) violates the constraint.
						 */
						net.addArc(aA, qaAB);
						if (edge.getTailNode().getHigh() <= 1) {
							net.addArc(paAB, aA);
						} else {
							net.addArc(qaAB, aA);
						}
					}
				}
			}
		}
	}

	private void addAlwaysBefore(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isAlwaysBefore()) {
			for (LogSkeletonEdge edge : graph.getEdges().values()) {
				if (edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
					// Tail (A) always before head (B)
					Transition aA = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition aB = configuration.isMerge() ? transitions.get(edge.getHeadNode())
							: net.addTransition(edge.getHeadNode().getLabel());
					if (configuration.isOptimizeAlwaysNever() && configuration.isOptimizeNeverEquivalence()
							&& configuration.isNever() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getTailType() == LogSkeletonEdgeType.NEVER
							&& edge.getTailNode().getLabelRepresentative()
									.equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Avoid duplication: addNever() will take care of this.
						 */
					} else if (configuration.isOptimizeAlwaysEquivalence() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getTailNode().getLabelRepresentative()
									.equals(edge.getHeadNode().getLabelRepresentative())) {
						if (configuration.isAlwaysAfter() && edge.getTailType() == LogSkeletonEdgeType.ALWAYS) {
							/*
							 * Avoid duplication: addAlwaysAfter() will take care of this.
							 */
						} else {
							/*
							 * Both A and B occur at most once, and always occur equally often. The pbAB
							 * place between them will do.
							 */
							Place pbAB = net.addPlace(PREFIX_BEFORE_P1 + edge.toString());
							net.addArc(aA, pbAB);
							net.addArc(pbAB, aB);
						}
					} else if (configuration.isOptimizeAlwaysElementary() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1) {
						Place pbAB = net.addPlace(PREFIX_BEFORE_P1 + edge.toString());
						Place qbAB = net.addPlace(PREFIX_BEFORE_P2 + edge.toString());
						Place rbAB = net.addPlace(PREFIX_BEFORE_P3 + edge.toString());
						if (configuration.isMarking()) {
							startMarking.add(pbAB);
							endMarking.add(rbAB);
						} else {
							net.addArc(startTransition, pbAB);
							net.addArc(rbAB, endTransition);
						}
						Transition tbAB = net.addTransition(PREFIX_BEFORE_T1 + edge.toString());
						tbAB.setInvisible(true);
						Transition ubAB = net.addTransition(PREFIX_BEFORE_T2 + edge.toString());
						ubAB.setInvisible(true);
						net.addArc(pbAB, aA);
						net.addArc(qbAB, tbAB);
						net.addArc(aA, qbAB);
						net.addArc(tbAB, rbAB);
						net.addArc(qbAB, aB);
						net.addArc(aB, rbAB);
						net.addArc(pbAB, ubAB);
						net.addArc(ubAB, rbAB);
					} else {
						/*
						 * Place qbAB models that the constraint is not satisfied: We need to do B.
						 */
						Place qbAB = net.addPlace(PREFIX_BEFORE_P2 + edge.toString());
						/*
						 * Place pbAB models that the constraint is satisfied.
						 */
						Place pbAB = net.addPlace(PREFIX_BEFORE_P1 + edge.toString());
						/*
						 * Initially, the constraint is satisfied.
						 */
						if (configuration.isMarking()) {
							startMarking.add(pbAB);
							endMarking.add(pbAB);
						} else {
							net.addArc(startTransition, pbAB);
							net.addArc(pbAB, endTransition);
						}
						/*
						 * Transition tbAB models two possibilities: 1. Either we needed to do A, which
						 * will not violate the constraint. 2. Or we needed to do B, which will not
						 * violate the constraint as well.
						 * 
						 * This seems a bit strange, but this works as follows: - In the initial state,
						 * we cannot do B. - After having fired A, the token will be in the qbAB place.
						 * - As long as the token is in the qbAB place, B can fire. - As long as the
						 * token is in the qbAB place, A can fire after the tbAB transition fires first.
						 * - At the end, the tbAB transition can fire to reach the end marking.
						 */
						Transition tbAB = net.addTransition(PREFIX_BEFORE_T1 + edge.toString());
						tbAB.setInvisible(true);
						net.addArc(pbAB, aA);
						net.addArc(aA, qbAB);
						net.addArc(qbAB, tbAB);
						net.addArc(tbAB, pbAB);
						net.addArc(qbAB, aB);
						if (edge.getHeadNode().getHigh() <= 1) {
							net.addArc(aB, pbAB);
						} else {
							net.addArc(aB, qbAB);
						}
					}
				}
			}
		}
	}

	private void addNever(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isNever()) {
			for (LogSkeletonEdge edge : graph.getEdges().values()) {
				if (edge.getHeadType() == LogSkeletonEdgeType.NEVER
						|| edge.getTailType() == LogSkeletonEdgeType.NEVER) {
					// Head (B) never before tail (A) or A never after B
					Transition aA = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition aB = configuration.isMerge() ? transitions.get(edge.getHeadNode())
							: net.addTransition(edge.getHeadNode().getLabel());
					if (configuration.isOptimizeNeverEquivalence() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getTailNode().getLabelRepresentative()
									.equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Both the tail and the head activity occur at most once, and always occur
						 * equally often. The p1 place between them will do.
						 */
						Place pnAB = net.addPlace(PREFIX_NEVER_P1 + edge.toString());
						net.addArc(aA, pnAB);
						net.addArc(pnAB, aB);
					} else if (configuration.isOptimizeNeverAlways()
							&& (edge.getHeadType() == LogSkeletonEdgeType.ALWAYS
									|| edge.getTailType() == LogSkeletonEdgeType.ALWAYS)
							&& (configuration.isAlwaysAfter() || configuration.isAlwaysBefore())
							&& edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1) {
						/*
						 * Skip, always after or always before will take care of this.
						 */
					} else {
						/*
						 * A token in place pnAB indicates that we can do A.
						 */
						Place pnAB = net.addPlace(PREFIX_NEVER_P1 + edge.toString());
						/*
						 * A token in place qnAB indicates that we can do B.
						 */
						Place qnAB = net.addPlace(PREFIX_NEVER_P2 + edge.toString());
						/*
						 * A token in place rnAB indicates that we have done all A's and now start doign
						 * all B's.
						 */
						Place rnAB = net.addPlace(PREFIX_NEVER_P3 + edge.toString());
						if (configuration.isMarking()) {
							startMarking.add(pnAB);
							endMarking.add(qnAB);
						} else {
							net.addArc(startTransition, pnAB);
							net.addArc(qnAB, endTransition);
						}
						if (edge.getTailNode().getLow() != 1 || edge.getTailNode().getHigh() != 1) {
							Transition tnAB = net.addTransition(PREFIX_NEVER_T1 + edge.toString());
							tnAB.setInvisible(true);
							net.addArc(pnAB, tnAB);
							net.addArc(tnAB, rnAB);
						}
						if (edge.getHeadNode().getLow() != 1 || edge.getHeadNode().getHigh() != 1) {
							Transition unAB = net.addTransition(PREFIX_NEVER_T2 + edge.toString());
							unAB.setInvisible(true);
							net.addArc(rnAB, unAB);
							net.addArc(unAB, qnAB);
						}
						net.addArc(pnAB, aA);
						net.addArc(aB, qnAB);
						if (edge.getTailNode().getHigh() <= 1) {
							net.addArc(aA, rnAB);
						} else {
							net.addArc(aA, pnAB);
						}
						if (edge.getHeadNode().getHigh() <= 1) {
							net.addArc(rnAB, aB);
						} else {
							net.addArc(qnAB, aB);
						}
					}
				}
			}
		}
	}

	private void addExclusive(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isExclusive()) {
			// Exclusive edges.
			// First, get all such edges.
			Set<Set<LogSkeletonNode>> exclusiveNodeSets = new HashSet<Set<LogSkeletonNode>>();
			for (LogSkeletonEdge edge : graph.getEdges().values()) {
				if ((edge.getTailType() != null && edge.getTailType() == LogSkeletonEdgeType.EXCLUSIVE)
						|| (edge.getHeadType() != null && edge.getHeadType() == LogSkeletonEdgeType.EXCLUSIVE)) {
					Set<LogSkeletonNode> exclusive = new HashSet<LogSkeletonNode>();
					exclusive.add(edge.getTailNode());
					exclusive.add(edge.getHeadNode());
					exclusiveNodeSets.add(exclusive);
				}
			}
			// Second, get all maximal sets of edges.
			Set<Set<LogSkeletonNode>> maximalNodeSets = new HashSet<Set<LogSkeletonNode>>();
			Set<Set<LogSkeletonNode>> workingNodeSets = new HashSet<Set<LogSkeletonNode>>(exclusiveNodeSets);
			while (!workingNodeSets.isEmpty()) {
				Set<LogSkeletonNode> workingNodes = workingNodeSets.iterator().next();
				boolean extended = false;
				for (LogSkeletonNode node : graph.getNodes()) {
					if (workingNodes.contains(node)) {
						continue;
					}
					boolean maximal = true;
					for (LogSkeletonNode workingNode : workingNodes) {
						Set<LogSkeletonNode> nodes = new HashSet<LogSkeletonNode>();
						nodes.add(workingNode);
						nodes.add(node);
						if (!exclusiveNodeSets.contains(nodes)) {
							maximal = false;
							break;
						}
					}
					if (maximal) {
						// We can extend the current set with this node.
						Set<LogSkeletonNode> extendedWorkingNodes = new HashSet<LogSkeletonNode>(workingNodes);
						extendedWorkingNodes.add(node);
						workingNodeSets.add(extendedWorkingNodes);
						extended = true;
					}
				}
				if (!extended) {
					// We cannot extend the current set with any other node.
					maximalNodeSets.add(workingNodes);
				}
				workingNodeSets.remove(workingNodes);
			}
			// Third, create a subnet for every maximal set.
			int i = 0;
			for (Set<LogSkeletonNode> maximalNodes : maximalNodeSets) {
				boolean canSkip = false;
				Place px = net.addPlace(PREFIX_EXCLUSIVE_P1 + i);
				Place qx = net.addPlace(PREFIX_EXCLUSIVE_P2 + i);
				if (configuration.isMarking()) {
					startMarking.add(px);
					endMarking.add(qx);
				} else {
					net.addArc(startTransition, px);
					net.addArc(qx, endTransition);
				}
				for (LogSkeletonNode node : maximalNodes) {
					Transition aX = configuration.isMerge() ? transitions.get(node)
							: net.addTransition(node.getLabel());
					if (node.getHigh() <= 1) {
						net.addArc(px, aX);
						net.addArc(aX, qx);
					} else {
						canSkip = true;
						Place rxX = net.addPlace(PREFIX_EXCLUSIVE_P3 + i);
						Transition uxX = net.addTransition(PREFIX_EXCLUSIVE_T2 + i);
						uxX.setInvisible(true);
						Transition vxX = net.addTransition(PREFIX_EXCLUSIVE_T3 + i);
						vxX.setInvisible(true);
						net.addArc(px, uxX);
						net.addArc(uxX, rxX);
						net.addArc(rxX, aX);
						net.addArc(aX, rxX);
						net.addArc(rxX, vxX);
						net.addArc(vxX, qx);
					}
					i++;
				}
				i++;
				if (!canSkip) {
					Transition tx = net.addTransition(PREFIX_EXCLUSIVE_T1);
					tx.setInvisible(true);
					net.addArc(px, tx);
					net.addArc(tx, qx);
				}
			}
		}
	}
}
