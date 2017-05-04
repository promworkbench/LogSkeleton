package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.SplitterAlgorithm;
import org.processmining.logskeleton.parameters.SplitterParameters;

@Plugin(name = "PDC 2017 Log 5 Splitter", parameterLabels = { "Event Log 5" }, returnLabels = { "Split Log 5" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log5SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		XLog filteredLog = log;
		for (int i = 0; i < 4; i++) {
			String suffix = "";
			for (int j = 0; j < i; j++) {
				suffix = suffix + ".1";
			}
			// Split a over itself
			parameters.getMilestoneActivities().clear();
			parameters.getMilestoneActivities().add("a");
			parameters.setDuplicateActivity("a");
			filteredLog = apply(filteredLog, parameters);
			// Split s over itself
			parameters.getMilestoneActivities().clear();
			parameters.getMilestoneActivities().add("i" + suffix);
			parameters.setDuplicateActivity("i" + suffix);
			filteredLog = apply(filteredLog, parameters);
			// Split t over itself
			parameters.getMilestoneActivities().clear();
			parameters.getMilestoneActivities().add("g" + suffix);
			parameters.setDuplicateActivity("g" + suffix);
			filteredLog = apply(filteredLog, parameters);
		}
		// Done
		return filteredLog;
	}
}
