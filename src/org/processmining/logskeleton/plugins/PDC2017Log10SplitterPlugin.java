package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.RecurrentActivitySplitterAlgorithm;
import org.processmining.logskeleton.parameters.RecurrentActivitySplitterParameters;

@Plugin(name = "PDC 2017 Unduplicate Log 10 Filter", parameterLabels = { "Event log"}, returnLabels = { "Filtered log" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log10SplitterPlugin extends RecurrentActivitySplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		RecurrentActivitySplitterParameters parameters = new RecurrentActivitySplitterParameters();
		// Split o over j
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("j");
		parameters.setDuplicateActivity("o");
		XLog filteredLog = apply(log, parameters);
		// Split i over j
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("j");
		parameters.setDuplicateActivity("i");
		filteredLog = apply(filteredLog, parameters);
		// Split q over q
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("q");
		parameters.setDuplicateActivity("q");
		filteredLog = apply(filteredLog, parameters);
		// Split j over j
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("j");
		parameters.setDuplicateActivity("j");
		filteredLog = apply(filteredLog, parameters);
		// Split g over q.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("q.1");
		parameters.setDuplicateActivity("g");
		filteredLog = apply(filteredLog, parameters);
		return filteredLog;
	}

}
