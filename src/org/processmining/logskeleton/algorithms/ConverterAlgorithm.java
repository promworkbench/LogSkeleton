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
		startTransition = net.addTransition("tstart");
		startTransition.setInvisible(true);
		endTransition = net.addTransition("tend");
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
				for (int i = 0; i < node.getHigh(); i++) {
					if (configuration.isMarking()) {
						endMarking.add(p3);
					} else {
						net.addArc(p3, endTransition);
					}
				}
				if (node.getLow() == node.getHigh()) {
					for (int i = 0; i < node.getHigh(); i++) {
						if (configuration.isMarking()) {
							startMarking.add(p1);
						} else {
							net.addArc(startTransition, p1);
						}
					}
				} else if (node.getLow() == 0) {
					for (int i = 0; i < node.getHigh(); i++) {
						if (configuration.isMarking()) {
							startMarking.add(p1);
						} else {
							net.addArc(startTransition, p1);
						}
					}
					Transition t1 = net.addTransition("t1" + node.getLabel());
					t1.setInvisible(true);
					net.addArc(p1, t1);
					net.addArc(t1, p3);
				} else {
					Place p2 = net.addPlace("p2" + node.getLabel());
					Transition t1 = net.addTransition("t1" + node.getLabel());
					t1.setInvisible(true);
					Transition t2 = net.addTransition("t2" + node.getLabel());
					t2.setInvisible(true);
					for (int i = 0; i < node.getLow(); i++) {
						if (configuration.isMarking()) {
							startMarking.add(p1);
						} else {
							net.addArc(startTransition, p1);
						}
					}
					for (int i = node.getLow(); i < node.getHigh(); i++) {
						if (configuration.isMarking()) {
							startMarking.add(p2);
						} else {
							net.addArc(startTransition, p2);
						}
					}
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
						Transition t1 = net.addTransition("t1" + node.getLabel());
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
					if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1 && edge.getTailNode()
							.getLabelRepresentative().equals(edge.getHeadNode().getLabelRepresentative())) {
						Place p1 = net.addPlace("p1e" + edge.toString());
						net.addArc(tt, p1);
						net.addArc(p1, th);
					} else if (configuration.isNever() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getHeadType() == LogSkeletonEdgeType.NEVER) {
						// NEVER will take care of this.
					} else {
						Place p1 = net.addPlace("p1a" + edge.toString());
						Place p2 = net.addPlace("p2a" + edge.toString());
						if (configuration.isMarking()) {
							startMarking.add(p2);
							endMarking.add(p2);
						} else {
							net.addArc(startTransition, p2);
							net.addArc(p2, endTransition);
						}
						Transition t1 = net.addTransition("t1a" + edge.toString());
						t1.setInvisible(true);
						net.addArc(p2, t1);
						net.addArc(t1, p1);
						net.addArc(p1, th);
						net.addArc(th, p2);
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
					if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1 && edge.getTailNode()
							.getLabelRepresentative().equals(edge.getHeadNode().getLabelRepresentative())) {
						Place p1 = net.addPlace("p1e" + edge.toString());
						net.addArc(tt, p1);
						net.addArc(p1, th);
					} else if (configuration.isNever() && edge.getTailNode().getHigh() <= 1
							&& edge.getHeadNode().getHigh() <= 1 && edge.getTailType() == LogSkeletonEdgeType.NEVER) {
						// NEVER will take care of this.
					} else {
						Place p1 = net.addPlace("p1b" + edge.toString());
						Place p2 = net.addPlace("p2b" + edge.toString());
						if (configuration.isMarking()) {
							startMarking.add(p2);
							endMarking.add(p2);
						} else {
							net.addArc(startTransition, p2);
							net.addArc(p2, endTransition);
						}
						Transition t1 = net.addTransition("t1b" + edge.toString());
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
							Transition t1 = net.addTransition("t1n" + edge.toString());
							t1.setInvisible(true);
							net.addArc(p1, t1);
							net.addArc(t1, p3);
						}
						if (edge.getHeadNode().getLow() != 1 || edge.getHeadNode().getHigh() != 1) {
							Transition t2 = net.addTransition("t2n" + edge.toString());
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
						Transition t1 = net.addTransition("t1" + i);
						t1.setInvisible(true);
						Transition t2 = net.addTransition("t2" + i);
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
					Transition t3 = net.addTransition("t3");
					t3.setInvisible(true);
					net.addArc(p1, t3);
					net.addArc(t3, p3);
				}
			}
		}
	}
}
