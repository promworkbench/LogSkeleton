package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.SplitterAlgorithm;
import org.processmining.logskeleton.parameters.SplitterParameters;

@Plugin(name = "PDC 2017 Log 9 Splitter", parameterLabels = { "Event Log 9"}, returnLabels = { "Split Log 9" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log9SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		// Split t over v
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("v");
		parameters.setDuplicateActivity("t");
		XLog filteredLog = apply(log, parameters);
		// Split ad over k
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("k");
		parameters.setDuplicateActivity("ad");
		filteredLog = apply(filteredLog, parameters);
		// Split ad.0 over ad.0
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("ad.0");
		parameters.setDuplicateActivity("ad.0");
		filteredLog = apply(filteredLog, parameters);
		// Split k over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("b");
		parameters.setDuplicateActivity("k");
		filteredLog = apply(filteredLog, parameters);
		// Split z over ad.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("ad.1");
		parameters.setDuplicateActivity("z");
		filteredLog = apply(filteredLog, parameters);
		// Split h over z.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("z.1");
		parameters.setDuplicateActivity("h");
		filteredLog = apply(filteredLog, parameters);
		// Split p over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("b");
		parameters.setDuplicateActivity("p");
		filteredLog = apply(filteredLog, parameters);
		// Split o over o
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("o");
		parameters.setDuplicateActivity("o");
		filteredLog = apply(filteredLog, parameters);
		return filteredLog;
	}

}
