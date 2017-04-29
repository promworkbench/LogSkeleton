package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.PDC2017LogFilterAlgorithm;

@Plugin(name = "Filter PDC 2017 Event Log", parameterLabels = { "PDC 2017 Event log"}, returnLabels = { "Filtered Event Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "Filter PDC 2017 Event Log")
public class PDC2017LogFilterPlugin extends PDC2017LogFilterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		return apply(log);
	}
}
