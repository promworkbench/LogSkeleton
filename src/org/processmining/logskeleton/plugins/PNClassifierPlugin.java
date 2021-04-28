package org.processmining.logskeleton.plugins;

import java.util.Collection;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.PNClassifierAlgorithm;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

@Plugin( //
		name = "Classify Log using Petri net converted from Log Skeleton", //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/blog/2019/09/20/classify-log-using-log-skeleton/", //
		parameterLabels = { "Log", "Petri net" }, //
		returnLabels = { "Classified Log" }, //
		returnTypes = { XLog.class }, //
		userAccessible = true, //
		help = "Classify Log using Petrinet converted from Log Skeleton." //
) //
public class PNClassifierPlugin extends PNClassifierAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Classify Log using Petri net converted from Log Skeleton", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public XLog run(UIPluginContext context, XLog log, Petrinet net) {
		return runHeadless(context, log, net);
	}
	
	@PluginVariant( //
			variantLabel = "Classify Log using Petri net converted from Log Skeleton", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public XLog runHeadless(PluginContext context, XLog log, Petrinet net) {
		XEventClassifier classifier = log.getClassifiers().size() > 0 ? log.getClassifiers().iterator().next() : new XEventNameClassifier();
		Marking initialMarking = null;
		Marking finalMarking = null;
		try {
			Collection<InitialMarkingConnection> connections = context.getConnectionManager().getConnections(
					InitialMarkingConnection.class, context, net);
			if (connections.size() > 0) {
				/*
				 * Found an initial marking, use it.
				 */
				initialMarking = (Marking) connections.iterator().next().getObjectWithRole(InitialMarkingConnection.MARKING);
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		try {
			Collection<FinalMarkingConnection> connections = context.getConnectionManager().getConnections(
					FinalMarkingConnection.class, context, net);
			if (connections.size() > 0) {
				/*
				 * Found a final marking, use it.
				 */
				finalMarking = (Marking) connections.iterator().next().getObjectWithRole(FinalMarkingConnection.MARKING);
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		if (initialMarking != null && finalMarking != null) {
			return apply(log, classifier, net, initialMarking, finalMarking);
		}
		return null;
	}
}
