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
		Place startPlace = net.addPlace("pstart");
		Place endPlace = net.addPlace("pend");
		/*
		 * We're using "{" as first character of a silent transition as this character
		 * follows all letters in the ASCII table. As a result, when sorted, the added 
		 * silent transitions will come last.
		 */
		startTransition = net.addTransition("{start}");
		startTransition.setInvisible(true);
		endTransition = net.addTransition("{end}");
		endTransition.setInvisible(true);
		net.addArc(startPlace, startTransition);
		net.addArc(endTransition, endPlace);

		startMarking = new Marking();
		startMarking.add(startPlace);
		endMarking = new Marking();
		endMarking.add(endPlace);
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
					transition.setInvisible(true);
					Place place = net.addPlace("p|>");
					net.addArc(startTransition, place);
					net.addArc(place, transition);
				} else if (node.getLabel().equals(LogSkeletonCount.ENDEVENT)) {
					// Artificial end activity, make transition silent.
					transition.setInvisible(true);
					Place place = net.addPlace("p[]");
					net.addArc(transition, place);
					net.addArc(place, endTransition);
				}
				transitions.put(node, transition);
			}
		}
	}

	private void addInterval(LogSkeletonGraph graph, ConverterConfiguration configuration) {
		if (configuration.isInterval()) {
			// Intervals
			for (LogSkeletonNode node : graph.getNodes()) {
				if (node.getLabel().equals(LogSkeletonCount.STARTEVENT)
						|| node.getLabel().equals(LogSkeletonCount.ENDEVENT)) {
					continue;
				}
				if (configuration.isEquivalence() && !node.getLabel().equals(node.getLabelRepresentative())) {
					continue;
				}
				Transition t = configuration.isMerge() ? transitions.get(node) : net.addTransition(node.getLabel());
				Place p1 = net.addPlace("p1" + node.getLabel());
				Place p3 = net.addPlace("p3" + node.getLabel());
				net.addArc(p1, t);
				net.addArc(t, p3);
				/*
				 * In the end, we want to have high tokens in the p3 place.
				 */
				if (configuration.isMarking()) {
					endMarking.add(p3, node.getHigh());
				} else {
					net.addArc(p3, endTransition, node.getHigh());
				}
				if (node.getLow() == node.getHigh()) {
					/*
					 * This activity always occurs high times. Put high tokens in the p1 place.
					 */
					if (configuration.isMarking()) {
						startMarking.add(p1, node.getHigh());
					} else {
						net.addArc(startTransition, p1, node.getHigh());
					}
				} else if (node.getLow() == 0) {
					/*
					 * This activity may not occur at all. Put high tokens in the p1 place.
					 */
					if (configuration.isMarking()) {
						startMarking.add(p1, node.getHigh());
					} else {
						net.addArc(startTransition, p1, node.getHigh());
					}
					/*
					 * Transition t1 skips the activity.
					 */
					Transition t1 = net.addTransition("{i}" + node.getLabel());
					t1.setInvisible(true);
					net.addArc(p1, t1);
					net.addArc(t1, p3);
				} else {
					/*
					 * This activity needs to occur at least low times, but at most high times.
					 */
					Place p2 = net.addPlace("p2" + node.getLabel());
					/*
					 * Transition t1 skips the activity.
					 */
					Transition t1 = net.addTransition("{i1}" + node.getLabel());
					t1.setInvisible(true);
					/*
					 * Transition t2 avoid skipping the activity.
					 */
					Transition t2 = net.addTransition("{i2}" + node.getLabel());
					t2.setInvisible(true);
					/*
					 * Add low tokens to the p2 place. This many times, the activity may be skipped.
					 */
					if (configuration.isMarking()) {
						startMarking.add(p1, node.getLow());
					} else {
						net.addArc(startTransition, p1, node.getLow());
					}
					/*
					 * Add the remaining high-low tokens in the p1 place. This many times, the
					 * activity may not be skipped.
					 */
					if (configuration.isMarking()) {
						startMarking.add(p2, node.getHigh() - node.getLow());
					} else {
						net.addArc(startTransition, p2, node.getHigh() - node.getLow());
					}
					/*
					 * Connect everything.
					 */
					net.addArc(p2, t2);
					net.addArc(t2, p1);
					net.addArc(p2, t1);
					net.addArc(t1, p3);
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
						Transition t1 = net.addTransition("{e}" + node.getLabel());
						t1.setInvisible(true);
						for (LogSkeletonNode requiredNode : requiredNodes) {
							Transition t = configuration.isMerge() ? transitions.get(requiredNode)
									: net.addTransition(requiredNode.getLabel());
							Place p1 = net.addPlace("p1" + requiredNode.getLabel());
							net.addArc(t, p1);
							net.addArc(p1, t1);
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
					// Head always after tail
					Transition tt = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition th = configuration.isMerge() ? transitions.get(edge.getHeadNode())
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
						 * Both the tail and the head activity occur at most once, and always occur
						 * equally often. The p1 place between them will do.
						 */
						Place p1 = net.addPlace("p1e" + edge.toString());
						net.addArc(tt, p1);
						net.addArc(p1, th);
					} else {
						/*
						 * Place p1 models that the constraint is not satisfied: We need to do the head
						 * activity.
						 */
						Place p1 = net.addPlace("p1a" + edge.toString());
						/*
						 * Place p2 models that the constraint is satisfied.
						 */
						Place p2 = net.addPlace("p2a" + edge.toString());
						/*
						 * Initially, the constraint is satisfied.
						 */
						if (configuration.isMarking()) {
							startMarking.add(p2);
							endMarking.add(p2);
						} else {
							net.addArc(startTransition, p2);
							net.addArc(p2, endTransition);
						}
						/*
						 * Transition t1 models two possibilities: 1. Either we need to do the tail
						 * activity, which will violate the constraint. 2. Or we need to do the head
						 * activity, which will not violate the constraint.
						 */
						Transition t1 = net.addTransition("{a}" + edge.toString());
						t1.setInvisible(true);
						/*
						 * Connect everything.
						 */
						net.addArc(p2, t1);
						net.addArc(t1, p1);
						/*
						 * Firing the head activity restores the constraint.
						 */
						net.addArc(p1, th);
						net.addArc(th, p2);
						/*
						 * Firing the tail activity violates the constraint.
						 */
						net.addArc(tt, p1);
						if (edge.getTailNode().getHigh() <= 1) {
							net.addArc(p2, tt);
						} else {
							net.addArc(p1, tt);
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
					// Tail always before head
					Transition tt = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition th = configuration.isMerge() ? transitions.get(edge.getHeadNode())
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
							 * Both the tail and the head activity occur at most once, and always occur
							 * equally often. The p1 place between them will do.
							 */
							Place p1 = net.addPlace("p1e" + edge.toString());
							net.addArc(tt, p1);
							net.addArc(p1, th);
						}
					} else {
						/*
						 * Place p1 models that the constraint is not satisfied: We need to do the head
						 * activity.
						 */
						Place p1 = net.addPlace("p1b" + edge.toString());
						/*
						 * Place p2 models that the constraint is satisfied.
						 */
						Place p2 = net.addPlace("p2b" + edge.toString());
						/*
						 * Initially, the constraint is satisfied.
						 */
						if (configuration.isMarking()) {
							startMarking.add(p2);
							endMarking.add(p2);
						} else {
							net.addArc(startTransition, p2);
							net.addArc(p2, endTransition);
						}
						/*
						 * Transition t1 models two possibilities: 1. Either we needed to do the tail
						 * activity, which will not violate the constraint. 2. Or we needed to do the
						 * tail activity, which will not violate the constraint as well.
						 * 
						 * This seems a bit strange, but this works as follows: - In the initial state,
						 * we cannot do the head transition. - After having fired the head transition,
						 * the token will be in the p1 place. - As long as the token is in the p1 place,
						 * the head transition can fire. - As long as the token is in the p1 place, the
						 * tail transition can fire after the t1 transition fires first. - At the end,
						 * the t1 transition can fire to reach the end marking.
						 */
						Transition t1 = net.addTransition("{b}" + edge.toString());
						t1.setInvisible(true);
						net.addArc(p2, tt);
						net.addArc(tt, p1);
						net.addArc(p1, t1);
						net.addArc(t1, p2);
						net.addArc(p1, th);
						if (edge.getHeadNode().getHigh() <= 1) {
							net.addArc(th, p2);
						} else {
							net.addArc(th, p1);
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
					// Head never before tail or tail never after head
					Transition tt = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition th = configuration.isMerge() ? transitions.get(edge.getHeadNode())
							: net.addTransition(edge.getHeadNode().getLabel());
					if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1 && edge.getTailNode()
							.getLabelRepresentative().equals(edge.getHeadNode().getLabelRepresentative())) {
						/*
						 * Both the tail and the head activity occur at most once, and always occur
						 * equally often. The p1 place between them will do.
						 */
						Place p1 = net.addPlace("p1e" + edge.toString());
						net.addArc(tt, p1);
						net.addArc(p1, th);
					} else {
						Place p1 = net.addPlace("p1n" + edge.toString());
						Place p2 = net.addPlace("p2n" + edge.toString());
						Place p3 = net.addPlace("p3n" + edge.toString());
						if (configuration.isMarking()) {
							startMarking.add(p1);
							endMarking.add(p2);
						} else {
							net.addArc(startTransition, p1);
							net.addArc(p2, endTransition);
						}
						if (edge.getTailNode().getLow() != 1 || edge.getTailNode().getHigh() != 1) {
							Transition t1 = net.addTransition("{n1}" + edge.toString());
							t1.setInvisible(true);
							net.addArc(p1, t1);
							net.addArc(t1, p3);
						}
						if (edge.getHeadNode().getLow() != 1 || edge.getHeadNode().getHigh() != 1) {
							Transition t2 = net.addTransition("{n2}" + edge.toString());
							t2.setInvisible(true);
							net.addArc(p3, t2);
							net.addArc(t2, p2);
						}
						net.addArc(p1, tt);
						net.addArc(th, p2);
						if (edge.getTailNode().getHigh() <= 1) {
							net.addArc(tt, p3);
						} else {
							net.addArc(tt, p1);
						}
						if (edge.getHeadNode().getHigh() <= 1) {
							net.addArc(p3, th);
						} else {
							net.addArc(p2, th);
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
				Place p1 = net.addPlace("p1" + i);
				Place p3 = net.addPlace("p3" + i);
				if (configuration.isMarking()) {
					startMarking.add(p1);
					endMarking.add(p3);
				} else {
					net.addArc(startTransition, p1);
					net.addArc(p3, endTransition);
				}
				for (LogSkeletonNode node : maximalNodes) {
					Transition t = configuration.isMerge() ? transitions.get(node) : net.addTransition(node.getLabel());
					if (node.getHigh() <= 1) {
						net.addArc(p1, t);
						net.addArc(t, p3);
					} else {
						canSkip = true;
						Place p2 = net.addPlace("p2" + i);
						Transition t1 = net.addTransition("{x1}" + i);
						t1.setInvisible(true);
						Transition t2 = net.addTransition("{x2}" + i);
						t2.setInvisible(true);
						net.addArc(p1, t1);
						net.addArc(t1, p2);
						net.addArc(p2, t);
						net.addArc(t, p2);
						net.addArc(p2, t2);
						net.addArc(t2, p3);
					}
					i++;
				}
				i++;
				if (!canSkip) {
					Transition t3 = net.addTransition("{x3}");
					t3.setInvisible(true);
					net.addArc(p1, t3);
					net.addArc(t3, p3);
				}
			}
		}
	}
}
