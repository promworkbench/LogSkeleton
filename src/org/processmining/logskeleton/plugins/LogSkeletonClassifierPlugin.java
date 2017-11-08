package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.algorithms.LogSkeletonClassifierAlgorithm;

@Plugin(name = "Classify Test Log using Log Skeleton", parameterLabels = { "Training Event log", "Test Event Log"}, returnLabels = { "Classified Event Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "Filter Event Log on Log Skeleton")
public class LogSkeletonClassifierPlugin extends LogSkeletonClassifierAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0, 1 })
	public XLog run(PluginContext context, XLog trainingLog, XLog testLog) {
		return apply(context, trainingLog, testLog, new LogPreprocessorAlgorithm());
	}

}
