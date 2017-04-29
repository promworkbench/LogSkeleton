package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.RecurrentActivitySplitterAlgorithm;
import org.processmining.logskeleton.parameters.RecurrentActivitySplitterParameters;

@Plugin(name = "PDC 2017 Log 4 Splitter", parameterLabels = { "Event log"}, returnLabels = { "Split Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log4SplitterPlugin extends RecurrentActivitySplitterAlgorithm {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		RecurrentActivitySplitterParameters parameters = new RecurrentActivitySplitterParameters();
		// Split t over l
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("l");
		parameters.setDuplicateActivity("t");
		XLog filteredLog = apply(log, parameters);
		// Split r over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("b");
		parameters.setDuplicateActivity("r");
		filteredLog = apply(filteredLog, parameters);
		// Split m over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("b");
		parameters.setDuplicateActivity("m");
		filteredLog = apply(filteredLog, parameters);
		// Done
		return filteredLog;
	}

}
