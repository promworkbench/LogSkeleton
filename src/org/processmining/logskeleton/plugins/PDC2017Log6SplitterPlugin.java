package org.processmining.logskeleton.plugins;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.SplitterAlgorithm;
import org.processmining.logskeleton.parameters.SplitterParameters;

@Plugin(name = "PDC 2017 Log 6 Splitter", parameterLabels = { "Event Log 6" }, returnLabels = { "Split Log 6" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log6SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		XLog filteredLog = log;
		// Split d over e
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e");
		parameters.setDuplicateActivity("d");
		filteredLog = apply(filteredLog, parameters);
		// Split h over e
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e");
		parameters.setDuplicateActivity("h");
		filteredLog = apply(filteredLog, parameters);
		// Split p over e
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e");
		parameters.setDuplicateActivity("p");
		filteredLog = apply(filteredLog, parameters);
		// Split e over itself
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e");
		parameters.setDuplicateActivity("e");
		filteredLog = apply(filteredLog, parameters);
		// Split d.1 over e.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e.1");
		parameters.setDuplicateActivity("d.1");
		filteredLog = apply(filteredLog, parameters);
		// Split h.1 over e.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e.1");
		parameters.setDuplicateActivity("h.1");
		filteredLog = apply(filteredLog, parameters);
		// Split p.1 over e.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e.1");
		parameters.setDuplicateActivity("p.1");
		filteredLog = apply(filteredLog, parameters);
		// Split e.1 over itself
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e.1");
		parameters.setDuplicateActivity("e.1");
		filteredLog = apply(filteredLog, parameters);
		// Done
		XConceptExtension.instance().assignName(
				filteredLog,
				XConceptExtension.instance().extractName(log)
						+ " | split: [d, e], [h, e], [p, e], [e, e], [d.1, e.1], [h.1, e.1], [p.1, e.1], [e.1, e.1]");
		return filteredLog;
	}
}
