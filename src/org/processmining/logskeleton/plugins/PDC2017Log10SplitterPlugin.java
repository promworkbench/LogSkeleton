package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.SplitterAlgorithm;
import org.processmining.logskeleton.parameters.SplitterParameters;

@Plugin(name = "PDC 2017 Log 10 Splitter", parameterLabels = { "Event Log 10"}, returnLabels = { "Split Log 10" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log10SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
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