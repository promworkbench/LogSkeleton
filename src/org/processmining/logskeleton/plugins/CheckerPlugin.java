package org.processmining.logskeleton.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.CheckerAlgorithm;
import org.processmining.logskeleton.configurations.CheckerConfiguration;
import org.processmining.logskeleton.inputs.CheckerInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.panels.CheckerPanel;

@Plugin( //
		name = "Filter Event Log on Log Skeleton", //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/blog/2019/09/20/filter-event-log-on-log-skeleton/", //
		parameterLabels = { "Log Skeleton", "Event log", "Configuration" }, //
		returnLabels = { "Filtered Event Log" }, //
		returnTypes = { XLog.class }, //
		userAccessible = true, //
		help = "Filter Event Log on Log Skeleton" //
) //
public class CheckerPlugin extends CheckerAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Filter Event Log on Log Skeleton user User Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public XLog run(UIPluginContext context, LogSkeleton logSkeleton, XLog log) {
		CheckerInput input = new CheckerInput(logSkeleton, log);
		CheckerConfiguration configuration = new CheckerConfiguration(input);
		CheckerPanel panel = new CheckerPanel(log, configuration);
		InteractionResult result = context.showWizard("Configure checker (classifier, options)", true, true, panel);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return apply(context, input, configuration).getLog();
	}

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Filter Event Log on Log Skeleton using Default Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public XLog run(PluginContext context, LogSkeleton logSkeleton, XLog log) {
		CheckerInput input = new CheckerInput(logSkeleton, log);
		CheckerConfiguration configuration = new CheckerConfiguration(input);
		return apply(context, input, configuration).getLog();
	}

	@PluginVariant( //
			variantLabel = "Filter Event Log on Log Skeleton using Provided Configuration", //
			requiredParameterLabels = { 0, 1, 2 } //
	) //
	public XLog run(PluginContext context, LogSkeleton logSkeleton, XLog log, CheckerConfiguration configuration) {
		CheckerInput input = new CheckerInput(logSkeleton, log);
		return apply(context, input, configuration).getLog();
	}
}
