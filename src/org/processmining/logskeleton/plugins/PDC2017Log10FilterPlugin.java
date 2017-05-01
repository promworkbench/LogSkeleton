package org.processmining.logskeleton.plugins;

import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogSkeletonBuilderAlgorithm;
import org.processmining.logskeleton.models.LogSkeletonCount;

@Plugin(name = "PDC 2017 Log 10 Filter", parameterLabels = { "Event Log 10"}, returnLabels = { "Filtered Log 10" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log10FilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		LogSkeletonBuilderAlgorithm skeletonBuilder = new LogSkeletonBuilderAlgorithm();
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount count = skeletonBuilder.count(traceLog);
			if (count.get("d") != 1) {
				continue;
			}
			filteredLog.add(trace);
		}
		return filteredLog;
	}
}