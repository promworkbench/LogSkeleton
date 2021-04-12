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

		Marking startMarking = new Marking();
		startMarking.add(startPlace);
		Marking endMarking = new Marking();
		endMarking.add(endPlace);

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
							if (configuration.isAlways() && candidateRequiredNode.getHigh() <= 1) {
								/*
								 * Check if there is an equivalent preceding node that has always after/before
								 * with this node. If so, this edge will take care of the equivalence check with
								 * that node.
								 */
								for (LogSkeletonEdge edge : candidateRequiredNode.getIncoming().values()) {
									if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS
											&& edge.getHeadType() == LogSkeletonEdgeType.ALWAYS
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

		if (configuration.isAlways() && configuration.isSkip()) {
			Map<LogSkeletonNode, Transition> skipMap = new HashMap<LogSkeletonNode, Transition>();
			for (LogSkeletonNode node : graph.getNodes()) {
				if (node.getLow() < node.getHigh()) {
					Transition tskip = net.addTransition("tskip" + node.getLabel());
					tskip.setInvisible(true);
					skipMap.put(node, tskip);
				}
			}
			for (LogSkeletonEdge edge : graph.getEdges().values()) {
				if (edge.getTailType() != LogSkeletonEdgeType.EXCLUSIVE
						&& edge.getHeadType() != LogSkeletonEdgeType.EXCLUSIVE) {
					Transition tt = configuration.isMerge() ? transitions.get(edge.getTailNode())
							: net.addTransition(edge.getTailNode().getLabel());
					Transition th = configuration.isMerge() ? transitions.get(edge.getHeadNode())
							: net.addTransition(edge.getHeadNode().getLabel());
					Transition st = skipMap.get(edge.getTailNode());
					Transition sh = skipMap.get(edge.getHeadNode());
					Place p1 = net.addPlace("p1" + edge.toString());
					if (edge.getTailNode().getHigh() <= 1 && edge.getHeadNode().getHigh() <= 1) {
						net.addArc(tt, p1);
						net.addArc(p1, th);
						if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS
								|| edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							if (st != null || sh != null) {
								if (edge.getHeadType() != LogSkeletonEdgeType.ALWAYS) {
									if (sh == null) {
										net.addArc(st, p1);
									} else {
										Transition t1 = net.addTransition("t1" + edge.toString());
										t1.setInvisible(true);
										Place p2 = net.addPlace("p2" + edge.toString());
										net.addArc(st, p2);
										net.addArc(p2, t1);
										net.addArc(p2, sh);
										net.addArc(t1, p1);
									}
								} else if (edge.getTailType() != LogSkeletonEdgeType.ALWAYS) {
									if (st == null) {
										net.addArc(p1, sh);
									} else {
										Transition t2 = net.addTransition("t2" + edge.toString());
										t2.setInvisible(true);
										Place p2 = net.addPlace("p2" + edge.toString());
										net.addArc(p1, t2);
										net.addArc(st, p2);
										net.addArc(t2, p2);
										net.addArc(p2, sh);
									}
								} else if (st != null && sh != null) {
									Place p2 = net.addPlace("p2" + edge.toString());
									net.addArc(st, p2);
									net.addArc(p2, sh);
								}
							}
						}
						if (edge.getTailType() == LogSkeletonEdgeType.NEVER
								&& edge.getHeadType() == LogSkeletonEdgeType.NEVER) {
							if (st != null) {
								net.addArc(st, p1);
							}
							if (sh != null) {
								net.addArc(p1, sh);
							}
						}
					} else if (edge.getTailNode().getLabelRepresentative().equals(edge.getHeadNode().getLabelRepresentative())) {
						net.addArc(tt, p1);
						net.addArc(p1, th);
						if (st != null && sh != null) {
							Place p2 = net.addPlace("p2" + edge.toString());
							net.addArc(st, p2);
							net.addArc(p2, sh);
						}
					} else if (edge.getTailType() == LogSkeletonEdgeType.NEVER
							|| edge.getHeadType() == LogSkeletonEdgeType.NEVER) {
						Transition t3 = net.addTransition("t3" + edge.toString());
						t3.setInvisible(true);
						Place p2 = net.addPlace("p2" + edge.toString());
						net.addArc(tt, p1);
						if (st != null) {
							net.addArc(st, p1);
						}
						net.addArc(p1, t3, edge.getTailNode().getHigh());
						if (edge.getHeadNode().getLow() == 0) {
							net.addArc(t3, p2, edge.getHeadNode().getHigh());
							net.addArc(p2, th);
							if (sh != null) {
								net.addArc(p2, sh);
							}
						} else if (sh == null) {
							net.addArc(t3, p2, edge.getHeadNode().getLow());
							net.addArc(p2, th);
						} else {
							Place p3 = net.addPlace("p3" + edge.toString());
							Transition t1 = net.addTransition("t1" + edge.toString());
							t1.setInvisible(true);
							net.addArc(t3, p2, edge.getHeadNode().getLow());
							net.addArc(t3, p3, edge.getHeadNode().getHigh() - edge.getHeadNode().getLow());
							net.addArc(p3, t1);
							net.addArc(t1, p2);
							net.addArc(p2, th);
							net.addArc(p3, sh);
						}
					} else {
						Transition t3 = net.addTransition("t3" + edge.toString());
						t3.setInvisible(true);
						Place p2 = net.addPlace("p2" + edge.toString());
						net.addArc(tt, p1);
						if (st != null) {
							net.addArc(st, p1);
						}
						net.addArc(p1, t3, edge.getTailNode().getHigh());
						int w = edge.getTailNode().getHigh();
						if (edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							w--;
						}
						if (configuration.isMarking()) {
							startMarking.add(p1, w);
							endMarking.add(p1, w);
						} else {
							net.addArc(startTransition, p1, w);
							net.addArc(p1, endTransition, w);
						}
						w = edge.getHeadNode().getHigh();
						if (edge.getTailType() == LogSkeletonEdgeType.ALWAYS) {
							w--;
						}
						if (configuration.isMarking()) {
							startMarking.add(p2, w);
							endMarking.add(p2, w);
						} else {
							net.addArc(startTransition, p2, w);
							net.addArc(p2, endTransition, w);
						}
						if (edge.getHeadNode().getLow() == 0) {
							net.addArc(t3, p2, edge.getHeadNode().getHigh());
							net.addArc(p2, th);
							if (sh != null) {
								net.addArc(p2, sh);
							}
						} else if (sh == null) {
							net.addArc(t3, p2, edge.getHeadNode().getLow());
							net.addArc(p2, th);
						} else {
							Place p3 = net.addPlace("p3" + edge.toString());
							Transition t1 = net.addTransition("t1" + edge.toString());
							t1.setInvisible(true);
							net.addArc(t3, p2, edge.getHeadNode().getLow());
							net.addArc(t3, p3, edge.getHeadNode().getHigh() - edge.getHeadNode().getLow());
							net.addArc(p3, t1);
							net.addArc(t1, p2);
							net.addArc(p2, th);
							net.addArc(p3, sh);
						}
					}
				}
			}
		} else if (configuration.isAlways()) {
			Map<String, Transition> skipMap = new HashMap<String, Transition>();
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
							//							Transition t3 = net.addTransition("t3" + edge.toString());
							//							t3.setInvisible(true);
							//							Transition t4 = net.addTransition("t4" + edge.toString());
							//							t4.setInvisible(true);
							if (!skipMap.containsKey(edge.getTailNode().getLabelRepresentative())) {
								Transition tskip = net.addTransition("tskip" + edge.toString());
								tskip.setInvisible(true);
								skipMap.put(edge.getTailNode().getLabelRepresentative(), tskip);
							}
							net.addArc(skipMap.get(edge.getTailNode().getLabelRepresentative()), p1);
							if (edge.getHeadNode().getLow() == 0) {
								if (!skipMap.containsKey(edge.getHeadNode().getLabelRepresentative())) {
									Transition tskip = net.addTransition("tskip" + edge.toString());
									tskip.setInvisible(true);
									skipMap.put(edge.getHeadNode().getLabelRepresentative(), tskip);
								}
								net.addArc(p1, skipMap.get(edge.getHeadNode().getLabelRepresentative()));
							}
							//							Place p2 = net.addPlace("p2" + edge.toString());
							//							Place p3 = net.addPlace("p3" + edge.toString());
							//							if (configuration.isMarking()) {
							//								startMarking.add(p2);
							//							} else {
							//								net.addArc(startTransition, p2);
							//							}
							//							net.addArc(p2, tt);
							//							net.addArc(p2, t4);
							//							net.addArc(p2, t3);
							net.addArc(tt, p1);
							//							net.addArc(t4, p1);
							net.addArc(p1, th);
							//							net.addArc(th, p3);
							//							net.addArc(t3, p3);
							//							if (configuration.isMarking()) {
							//								endMarking.add(p3);
							//							} else {
							//								net.addArc(p3, endTransition);
							//							}
						} else if (edge.getTailType() != LogSkeletonEdgeType.ALWAYS
								&& edge.getHeadType() == LogSkeletonEdgeType.ALWAYS) {
							//							Transition t3 = net.addTransition("t3" + edge.toString());
							//							t3.setInvisible(true);
							//							Transition t5 = net.addTransition("t5" + edge.toString());
							//							t5.setInvisible(true);
							if (edge.getTailNode().getLow() == 0) {
								if (!skipMap.containsKey(edge.getTailNode().getLabelRepresentative())) {
									Transition tskip = net.addTransition("tskip" + edge.toString());
									tskip.setInvisible(true);
									skipMap.put(edge.getTailNode().getLabelRepresentative(), tskip);
								}
								net.addArc(skipMap.get(edge.getTailNode().getLabelRepresentative()), p1);
							}
							if (!skipMap.containsKey(edge.getHeadNode().getLabelRepresentative())) {
								Transition tskip = net.addTransition("tskip" + edge.toString());
								tskip.setInvisible(true);
								skipMap.put(edge.getHeadNode().getLabelRepresentative(), tskip);
							}
							net.addArc(p1, skipMap.get(edge.getHeadNode().getLabelRepresentative()));
							//							Place p2 = net.addPlace("p2" + edge.toString());
							//							Place p3 = net.addPlace("p3" + edge.toString());
							//							if (configuration.isMarking()) {
							//								startMarking.add(p2);
							//							} else {
							//								net.addArc(startTransition, p2);
							//							}
							//							net.addArc(p2, tt);
							//							net.addArc(p2, t3);
							net.addArc(tt, p1);
							net.addArc(p1, th);
							//							net.addArc(p1, t5);
							//							net.addArc(t3, p3);
							//							net.addArc(th, p3);
							//							net.addArc(t5, p3);
							//							if (configuration.isMarking()) {
							//								endMarking.add(p3);
							//							} else {
							//								net.addArc(p3, endTransition);
							//							}
						} else if (edge.getTailType() == LogSkeletonEdgeType.NEVER
								|| edge.getHeadType() == LogSkeletonEdgeType.NEVER) {
							//							Transition t3 = net.addTransition("t3" + edge.toString());
							//							t3.setInvisible(true);
							//							Transition t4 = net.addTransition("t4" + edge.toString());
							//							t4.setInvisible(true);
							//							Transition t5 = net.addTransition("t5" + edge.toString());
							//							t5.setInvisible(true);
							if (!skipMap.containsKey(edge.getTailNode().getLabelRepresentative())) {
								Transition tskip = net.addTransition("tskip" + edge.toString());
								tskip.setInvisible(true);
								skipMap.put(edge.getTailNode().getLabelRepresentative(), tskip);
							}
							net.addArc(skipMap.get(edge.getTailNode().getLabelRepresentative()), p1);
							if (!skipMap.containsKey(edge.getHeadNode().getLabelRepresentative())) {
								Transition tskip = net.addTransition("tskip" + edge.toString());
								tskip.setInvisible(true);
								skipMap.put(edge.getHeadNode().getLabelRepresentative(), tskip);
							}
							net.addArc(p1, skipMap.get(edge.getHeadNode().getLabelRepresentative()));
							//							Place p2 = net.addPlace("p2" + edge.toString());
							//							Place p3 = net.addPlace("p3" + edge.toString());
							//							if (configuration.isMarking()) {
							//								startMarking.add(p2);
							//							} else {
							//								net.addArc(startTransition, p2);
							//							}
							//							net.addArc(p2, tt);
							//							net.addArc(p2, t4);
							//							net.addArc(p2, t3);
							net.addArc(tt, p1);
							//							net.addArc(t4, p1);
							net.addArc(p1, th);
							//							net.addArc(p1, t5);
							//							net.addArc(t3, p3);
							//							net.addArc(th, p3);
							//							net.addArc(t5, p3);
							//							if (configuration.isMarking()) {
							//								endMarking.add(p3);
							//							} else {
							//								net.addArc(p3, endTransition);
							//							}
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
						if (configuration.isMarking()) {
							startMarking.add(p2);
						} else {
							net.addArc(startTransition, p2);
						}
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
						if (configuration.isMarking()) {
							endMarking.add(p3);
						} else {
							net.addArc(p3, endTransition);
						}
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

		context.getProvidedObjectManager().createProvidedObject("Initial marking for " + net.getLabel(), startMarking,
				Marking.class, context);
		context.addConnection(new InitialMarkingConnection(net, startMarking));
		context.getProvidedObjectManager().createProvidedObject("Final marking for " + net.getLabel(), endMarking,
				Marking.class, context);
		context.addConnection(new FinalMarkingConnection(net, endMarking));

		return net;
	}
}
