package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogSkeletonCheckerAlgorithm;
import org.processmining.logskeleton.models.LogSkeleton;

@Plugin(name = "Filter Event Log on Log Skeleton", parameterLabels = { "Log Skeleton", "Event log"}, returnLabels = { "Filtered Event Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "Filter Event Log on Lok Skeleton")
public class LogSkeletonCheckerPlugin extends LogSkeletonCheckerAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0, 1 })
	public XLog run(PluginContext context, LogSkeleton model, XLog log) {
		return apply(model, log);
	}
}