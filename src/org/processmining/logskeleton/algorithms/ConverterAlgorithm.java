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
			Place startPlace = net.addPlace("{pi}");
			Place endPlace = net.addPlace("{po}");
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
					Place place = net.addPlace("{pi}");
					startMarking.add(place);
					net.addArc(place, transition);
				} else if (node.getLabel().equals(LogSkeletonCount.ENDEVENT)) {
					// Artificial end activity, make transition silent.
					endTransition = transition;
					transition.setInvisible(true);
					Place place = net.addPlace("{po}");
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
				Place piX = net.addPlace("{pi}" + node.getLabel());
				Place qiX = net.addPlace("{qi}" + node.getLabel());
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
					Transition tX = net.addTransition("{ti}" + node.getLabel());
					tX.setInvisible(true);
					net.addArc(piX, tX);
					net.addArc(tX, qiX);
				} else {
					/*
					 * Activity A needs to occur at least low times, but at most high times.
					 */
					Place riX = net.addPlace("{ri}" + node.getLabel());
					/*
					 * Transition t1A skips the activity A.
					 */
					Transition tiX = net.addTransition("{ti}" + node.getLabel());
					tiX.setInvisible(true);
					/*
					 * Transition uiA avoids skipping the activity A.
					 */
					Transition uiX = net.addTransition("{ui}" + node.getLabel());
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
							if (configuration.isAlwaysAfter() && candidateRequiredNode.getHigh() <= 1) {
								/*
								 * Check if there is an equivalent preceding node that has always after with
								 * this node. If so, this edge will take care of the equivalence check with that
								 * node.
								 */
								for (LogSkeletonEdge edge : candidateRequiredNode.getIncoming().values()) {
									if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS && node.getHigh() <= 1
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
							if (configuration.isAlwaysBefore() && candidateRequiredNode.getHigh() <= 1) {
								/*
								 * Check if there is an equivalent preceding node that has always before with
								 * this node. If so, this edge will take care of the equivalence check with that
								 * node.
								 */
								for (LogSkeletonEdge edge : candidateRequiredNode.getIncoming().values()) {
									if (edge.getHeadType() == LogSkeletonEdgeType.ALWAYS && node.getHigh() <= 1
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
							if (configuration.isNever() && candidateRequiredNode.getHigh() <= 1) {
								/*
								 * Check if there is an equivalent preceding node that has never after/before
								 * with this node. If so, this edge will take care of the equivalence check with
								 * that node.
								 */
								for (LogSkeletonEdge edge : candidateRequiredNode.getIncoming().values()) {
									if ((edge.getTailType() == LogSkeletonEdgeType.NEVER
											|| edge.getHeadType() == LogSkeletonEdgeType.NEVER) && node.getHigh() <= 1
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
						Transition teA = net.addTransition("{te}" + node.getLabel());
						teA.setInvisible(true);
						for (LogSkeletonNode requiredNode : requiredNodes) {
							Transition aX = configuration.isMerge() ? transitions.get(requiredNode)
									: net.addTransition(requiredNode.getLabel());
							Place peX = net.addPlace("{pe}" + requiredNode.getLabel());
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
					if (configuration.isNever() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getHeadType() == LogSkeletonEdgeType.NEVER) {
						/*
						 * Avoid duplication: addNever() will take care of this.
						 */
					} else if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1
							&& edge.getTailNode().getLabelRepresentative()
									.equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Both A and B occur at most once, and always occur equally often. The paAB
						 * place between them will do.
						 */
						Place paAB = net.addPlace("{pa}" + edge.toString());
						net.addArc(aA, paAB);
						net.addArc(paAB, aB);
					} else if (edge.getTailNode().getLabelRepresentative()
							.equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Simplification: Both A and B always occur equally often. The paAB place
						 * between them will do.
						 */
						Place paAB = net.addPlace("{pa}" + edge.toString());
						net.addArc(aA, paAB);
						net.addArc(paAB, aB);
					} else {
						/*
						 * Place qaAB models that the constraint is not satisfied: We need to do B.
						 */
						Place qaAB = net.addPlace("{qa}" + edge.toString());
						/*
						 * Place paAB models that the constraint is satisfied.
						 */
						Place paAB = net.addPlace("{pa}" + edge.toString());
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
						Transition taAB = net.addTransition("{ta}" + edge.toString());
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
					// Tail (B) always before head (A)
					Transition aA = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition aB = configuration.isMerge() ? transitions.get(edge.getHeadNode())
							: net.addTransition(edge.getHeadNode().getLabel());
					if (configuration.isNever() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getTailType() == LogSkeletonEdgeType.NEVER) {
						/*
						 * Avoid duplication: addNever() will take care of this.
						 */
					} else if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1
							&& edge.getTailNode().getLabelRepresentative()
									.equals(edge.getHeadNode().getLabelRepresentative())) {
						if (configuration.isAlwaysAfter()) {
							/*
							 * Avoid duplication: addAlwaysAfter() will take care of this.
							 */
						} else {
							/*
							 * Both A and B occur at most once, and always occur equally often. The pbAB
							 * place between them will do.
							 */
							Place pbAB = net.addPlace("{pb}" + edge.toString());
							net.addArc(aA, pbAB);
							net.addArc(pbAB, aB);
						}
					} else if (edge.getTailNode().getLabelRepresentative()
							.equals(edge.getHeadNode().getLabelRepresentative())) {
						if (configuration.isAlwaysAfter()) {
							/*
							 * Avoid duplication: addAlwaysAfter() will take care of this.
							 */
						} else {
							/*
							 * Simplification: Both A and B always occur equally often. The pbAB place
							 * between them will do.
							 */
							Place pbAB = net.addPlace("{pb}" + edge.toString());
							net.addArc(aA, pbAB);
							net.addArc(pbAB, aB);
						}
					} else {
						/*
						 * Place qbAB models that the constraint is not satisfied: We need to do B.
						 */
						Place qbAB = net.addPlace("{qb}" + edge.toString());
						/*
						 * Place pbAB models that the constraint is satisfied.
						 */
						Place pbAB = net.addPlace("{pb}" + edge.toString());
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
						Transition tbAB = net.addTransition("{tb}" + edge.toString());
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
					if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1 && edge.getTailNode()
							.getLabelRepresentative().equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Both the tail and the head activity occur at most once, and always occur
						 * equally often. The p1 place between them will do.
						 */
						Place pnAB = net.addPlace("{pn}" + edge.toString());
						net.addArc(aA, pnAB);
						net.addArc(pnAB, aB);
					} else {
						/*
						 * A token in place pnAB indicates that we can do A.
						 */
						Place pnAB = net.addPlace("{pn}" + edge.toString());
						/*
						 * A token in place qnAB indicates that we can do B.
						 */
						Place qnAB = net.addPlace("{qn}" + edge.toString());
						/*
						 * A token in place rnAB indicates that we have done all A's and now start doign
						 * all B's.
						 */
						Place rnAB = net.addPlace("{rn}" + edge.toString());
						if (configuration.isMarking()) {
							startMarking.add(pnAB);
							endMarking.add(qnAB);
						} else {
							net.addArc(startTransition, pnAB);
							net.addArc(qnAB, endTransition);
						}
						if (edge.getTailNode().getLow() != 1 || edge.getTailNode().getHigh() != 1) {
							Transition tnAB = net.addTransition("{tn}" + edge.toString());
							tnAB.setInvisible(true);
							net.addArc(pnAB, tnAB);
							net.addArc(tnAB, rnAB);
						}
						if (edge.getHeadNode().getLow() != 1 || edge.getHeadNode().getHigh() != 1) {
							Transition unAB = net.addTransition("{un}" + edge.toString());
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
				Place px = net.addPlace("px" + i);
				Place qx = net.addPlace("qx" + i);
				if (configuration.isMarking()) {
					startMarking.add(px);
					endMarking.add(qx);
				} else {
					net.addArc(startTransition, px);
					net.addArc(qx, endTransition);
				}
				for (LogSkeletonNode node : maximalNodes) {
					Transition aX = configuration.isMerge() ? transitions.get(node) : net.addTransition(node.getLabel());
					if (node.getHigh() <= 1) {
						net.addArc(px, aX);
						net.addArc(aX, qx);
					} else {
						canSkip = true;
						Place rxX = net.addPlace("rx" + i);
						Transition uxX = net.addTransition("{ux}" + i);
						uxX.setInvisible(true);
						Transition vxX = net.addTransition("{vx}" + i);
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
					Transition tx = net.addTransition("{tx}");
					tx.setInvisible(true);
					net.addArc(px, tx);
					net.addArc(tx, qx);
				}
			}
		}
	}
}
