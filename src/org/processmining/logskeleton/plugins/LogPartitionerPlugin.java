package org.processmining.logskeleton.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.models.EventLogArray;
import org.processmining.logskeleton.algorithms.LogPartitionerAlgorithm;
import org.processmining.logskeleton.configurations.LogPartitionerConfiguration;
import org.processmining.logskeleton.panels.LogPartitionerPanel;

@Plugin( //
		name = "Partition Log on Activity Sets", //
		parameterLabels = { "Event log", "Configuration" }, //
		returnLabels = { "Event Log Array" }, //
		returnTypes = { EventLogArray.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/blog/2019/09/17/partition-log-on-activity-sets/", //
		help = "Partition Event Log on Activity Sets" //
) //
public class LogPartitionerPlugin extends LogPartitionerAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Partition Log on Activity Sets using User Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public EventLogArray run(UIPluginContext context, XLog log) {
		LogPartitionerConfiguration configuration = new LogPartitionerConfiguration(log);
		LogPartitionerPanel panel = new LogPartitionerPanel(log, configuration);
		InteractionResult result = context.showWizard("Configure partitioning (classifier)", true, true, panel);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return run(context, log, configuration);
	}

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Partition Log on Activity Sets using Default Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public EventLogArray run(PluginContext context, XLog log) {
		return run(context, log, new LogPartitionerConfiguration(log));
	}

	@PluginVariant( //
			variantLabel = "Partition Log on Activity Sets using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public EventLogArray run(PluginContext context, XLog log, LogPartitionerConfiguration configuration) {
		EventLogArray logs = apply(log, configuration);
		for (int i = 0; i < logs.getSize(); i++) {
			context.getProvidedObjectManager().createProvidedObject(
					XConceptExtension.instance().extractName(logs.getLog(i)), logs.getLog(i), XLog.class, context);
		}
		return logs;
	}

}
