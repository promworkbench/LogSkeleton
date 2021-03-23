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

	public Petrinet apply(PluginContext context, LogSkeletonGraph graph, ConverterConfiguration configuration) {
		Petrinet net = PetrinetFactory.newPetrinet("Petri net converted from log skeleton " + graph.getTitle());

		Map<LogSkeletonNode, Transition> transitions = new HashMap<LogSkeletonNode, Transition>();
		Place startPlace = net.addPlace("pstart");
		Place endPlace = net.addPlace("pend");
		Transition startTransition = net.addTransition("tstart");
		startTransition.setInvisible(true);
		Transition endTransition = net.addTransition("tend");
		endTransition.setInvisible(true);
		net.addArc(startPlace, startTransition);
		net.addArc(endTransition, endPlace);

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

		if (configuration.isInterval()) {
			// Intervals
			for (LogSkeletonNode node : graph.getNodes()) {
				if (node.getLabel().equals(LogSkeletonCount.STARTEVENT)
						|| node.getLabel().equals(LogSkeletonCount.ENDEVENT)) {
					continue;
				}
				if (!node.getLabel().equals(node.getLabelRepresentative())) {
					continue;
				}
				Transition t = configuration.isMerge() ? transitions.get(node) : net.addTransition(node.getLabel());
				Place p1 = net.addPlace("p1" + node.getLabel());
				net.addArc(p1, t);
				if (node.getLow() == node.getHigh()) {
					for (int i = 0; i < node.getHigh(); i++) {
						net.addArc(startTransition, p1);
					}
				} else if (node.getLow() == 0) {
					for (int i = 0; i < node.getHigh(); i++) {
						net.addArc(startTransition, p1);
					}
					Transition t1 = net.addTransition("t1" + node.getLabel());
					t1.setInvisible(true);
					net.addArc(p1, t1);
				} else {
					Place p2 = net.addPlace("p2" + node.getLabel());
					Transition t1 = net.addTransition("t1" + node.getLabel());
					t1.setInvisible(true);
					Transition t2 = net.addTransition("t2" + node.getLabel());
					t2.setInvisible(true);
					for (int i = 0; i < node.getLow(); i++) {
						net.addArc(startTransition, p1);
					}
					for (int i = node.getLow(); i < node.getHigh(); i++) {
						net.addArc(startTransition, p2);
					}
					net.addArc(p2, t2);
					net.addArc(t2, p1);
					net.addArc(p2, t1);
				}
			}
		}

		if (configuration.isEquivalence()) {
			// Equivalence
			for (LogSkeletonNode node : graph.getNodes()) {
				if (node.getLabelRepresentative().equals(node.getLabel())) {
					Transition t1 = net.addTransition("t1" + node.getLabel());
					t1.setInvisible(true);
					for (LogSkeletonNode node2 : graph.getNodes()) {
						boolean ok = false;
						if (node2.getHigh() <= 1) {
							/*
							 * Check if there is an equivalent preceding node that has always after/before
							 * with this node. If so, this edge will take care of the equivalence check with that node.
							 */
							for (LogSkeletonEdge edge : node2.getIncoming().values()) {
								if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS
										&& edge.getHeadType() == LogSkeletonEdgeType.ALWAYS
										&& edge.getTailNode().getLabelRepresentative().equals(node.getLabel())) {
									ok = true;
									break;
								}
							}
						}
						if (ok) {
							continue;
						}
						if (node2.getLabelRepresentative().equals(node.getLabel())) {
							Transition t = configuration.isMerge() ? transitions.get(node2)
									: net.addTransition(node2.getLabel());
							Place p1 = net.addPlace("p1" + node2.getLabel());
							net.addArc(t, p1);
							net.addArc(p1, t1);
						}
					}
				}
			}
		}

		if (configuration.isAlways()) {
			// Non-exclusive edges
			for (LogSkeletonEdge edge : graph.getEdges().values()) {
				if (edge.getTailType() != LogSkeletonEdgeType.EXCLUSIVE
						&& edge.getHeadType() != LogSkeletonEdgeType.EXCLUSIVE) {
					Transition tt = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition th = configuration.isMerge() ? transitions.get(edge.getHeadNode())
							: net.addTransition(edge.getHeadNode().getLabel());
					Place p1 = net.addPlace("p1" + edge.toString());
					if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1) {
						if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS
								&& edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							net.addArc(tt, p1);
							net.addArc(p1, th);
						} else if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS
								&& edge.getHeadType() != LogSkeletonEdgeType.ALWAYS) {
							net.addArc(tt, p1);
							net.addArc(p1, th);
							Transition t4 = net.addTransition("t4" + edge.toString());
							t4.setInvisible(true);
							net.addArc(t4, p1);
						} else if (edge.getTailType() != LogSkeletonEdgeType.ALWAYS
								&& edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							net.addArc(tt, p1);
							net.addArc(p1, th);
							Transition t5 = net.addTransition("t5" + edge.toString());
							t5.setInvisible(true);
							net.addArc(p1, t5);
						} else if (edge.getTailType() == LogSkeletonEdgeType.NEVER
								|| edge.getHeadType() == LogSkeletonEdgeType.NEVER) {
							net.addArc(tt, p1);
							net.addArc(p1, th);
							Transition t4 = net.addTransition("t4" + edge.toString());
							t4.setInvisible(true);
							net.addArc(t4, p1);
							Transition t5 = net.addTransition("t5" + edge.toString());
							t5.setInvisible(true);
							net.addArc(p1, t5);
						}
					} else {
						Transition t1 = net.addTransition("t1" + edge.toString());
						t1.setInvisible(true);
						Transition t2 = net.addTransition("t2" + edge.toString());
						t2.setInvisible(true);
						Transition t3 = net.addTransition("t3" + edge.toString());
						t3.setInvisible(true);
						Transition t7 = net.addTransition("t7" + edge.toString());
						t7.setInvisible(true);
						Place p2 = net.addPlace("p2" + edge.toString());
						Place p3 = net.addPlace("p3" + edge.toString());
						Place p5 = net.addPlace("p5" + edge.toString());
						net.addArc(startTransition, p2);
						net.addArc(p2, t3);
						net.addArc(p2, t7);
						net.addArc(t7, p5);
						net.addArc(p5, tt);
						net.addArc(tt, p1);
						net.addArc(t7, p5);
						net.addArc(p1, t1);
						net.addArc(t1, p5);
						net.addArc(t3, p3);
						net.addArc(th, p3);
						net.addArc(p3, t2);
						net.addArc(p3, endTransition);
						if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS
								&& edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							net.addArc(p1, th);
							net.addArc(t2, p1);
						} else if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS && edge.getHeadType() == null) {
							Transition t4 = net.addTransition("t4" + edge.toString());
							t4.setInvisible(true);
							net.addArc(p1, th);
							net.addArc(t2, p1);
							net.addArc(p5, t4);
							net.addArc(t4, p1);
						} else if (edge.getTailType() == null && edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							Transition t5 = net.addTransition("t5" + edge.toString());
							t5.setInvisible(true);
							net.addArc(p1, th);
							net.addArc(t2, p1);
							net.addArc(p1, t5);
							net.addArc(t5, p3);
						} else if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS
								&& edge.getHeadType() == LogSkeletonEdgeType.NEVER) {
							Transition t4 = net.addTransition("t4" + edge.toString());
							t4.setInvisible(true);
							Transition t6 = net.addTransition("t6" + edge.toString());
							t6.setInvisible(true);
							Place p4 = net.addPlace("p4" + edge.toString());
							net.addArc(p1, t6);
							net.addArc(t6, p4);
							net.addArc(t2, p4);
							net.addArc(p4, th);
							net.addArc(p5, t4);
							net.addArc(t4, p1);
						} else if (edge.getTailType() == LogSkeletonEdgeType.NEVER
								&& edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							Transition t5 = net.addTransition("t5" + edge.toString());
							t5.setInvisible(true);
							Transition t6 = net.addTransition("t6" + edge.toString());
							t6.setInvisible(true);
							Place p4 = net.addPlace("p4" + edge.toString());
							net.addArc(p1, t6);
							net.addArc(t6, p4);
							net.addArc(t2, p4);
							net.addArc(p4, th);
							net.addArc(p4, t5);
							net.addArc(t5, p3);
						}
					}
				}
			}
		}

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
				net.addArc(startTransition, p1);
				net.addArc(p3, endTransition);
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

		Marking startMarking = new Marking();
		startMarking.add(startPlace);
		context.getProvidedObjectManager().createProvidedObject("Initial marking for " + net.getLabel(), startMarking,
				Marking.class, context);
		context.addConnection(new InitialMarkingConnection(net, startMarking));
		Marking endMarking = new Marking();
		endMarking.add(endPlace);
		context.getProvidedObjectManager().createProvidedObject("Final marking for " + net.getLabel(), endMarking,
				Marking.class, context);
		context.addConnection(new FinalMarkingConnection(net, endMarking));

		return net;
	}
}
