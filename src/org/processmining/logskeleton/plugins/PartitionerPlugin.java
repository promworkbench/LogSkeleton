package org.processmining.logskeleton.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.models.EventLogArray;
import org.processmining.logskeleton.algorithms.PartitionerAlgorithm;
import org.processmining.logskeleton.configurations.PartitionerConfiguration;
import org.processmining.logskeleton.inputs.PartitionerInput;
import org.processmining.logskeleton.panels.PartitionerPanel;

@Plugin( //
		name = "Partition Log on Activity Sets", //
		parameterLabels = { "Event log", "Configuration" }, //
		returnLabels = { "Event Log Array" }, //
		returnTypes = { EventLogArray.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/blog/2019/09/17/partition-log-on-activity-sets/", //
		help = "Partition Event Log on Activity Sets. Click the icon on the right for additional information." //
) //
public class PartitionerPlugin extends PartitionerAlgorithm {

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
		PartitionerInput input = new PartitionerInput(log);
		PartitionerConfiguration configuration = new PartitionerConfiguration(input);
		PartitionerPanel panel = new PartitionerPanel(log, configuration);
		InteractionResult result = context.showWizard("Configure partitioning (classifier, options)", true, true, panel);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return apply(context, input, configuration).getLogs();
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
		PartitionerInput input = new PartitionerInput(log);
		PartitionerConfiguration configuration = new PartitionerConfiguration(input);
		return apply(context, input, configuration).getLogs();
	}

	@PluginVariant( //
			variantLabel = "Partition Log on Activity Sets using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public EventLogArray run(PluginContext context, XLog log, PartitionerConfiguration configuration) {
		PartitionerInput input = new PartitionerInput(log);
		return apply(context, input, configuration).getLogs();
	}

}
