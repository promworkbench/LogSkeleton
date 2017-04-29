package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.RecurrentActivitySplitterAlgorithm;
import org.processmining.logskeleton.parameters.RecurrentActivitySplitterParameters;

@Plugin(name = "PDC 2017 Log 7 Splitter", parameterLabels = { "Event Log 7"}, returnLabels = { "Split Log 7" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log7SplitterPlugin extends RecurrentActivitySplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		RecurrentActivitySplitterParameters parameters = new RecurrentActivitySplitterParameters();
		// Split n over f
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("f");
		parameters.setDuplicateActivity("n");
		XLog filteredLog = apply(log, parameters);
		// Split h over i
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("i");
		parameters.setDuplicateActivity("h");
		filteredLog = apply(filteredLog, parameters);
		// Split c over i
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("i");
		parameters.setDuplicateActivity("c");
		filteredLog = apply(filteredLog, parameters);
		// Split h.0 over c.0
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("c.0");
		parameters.getMilestoneActivities().add("h.0");
		parameters.setDuplicateActivity("h.0");
		filteredLog = apply(filteredLog, parameters);
		// Split p over e
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e");
		parameters.setDuplicateActivity("p");
		filteredLog = apply(filteredLog, parameters);
		// Split c.0 over h.0.0
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("h.0.0");
		parameters.setDuplicateActivity("c.0");
		filteredLog = apply(filteredLog, parameters);
		// Done, except for b...
		return apply7B(filteredLog);
	}

}
