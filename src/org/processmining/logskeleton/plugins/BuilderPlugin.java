package org.processmining.logskeleton.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.BuilderAlgorithm;
import org.processmining.logskeleton.configurations.BuilderConfiguration;
import org.processmining.logskeleton.inputs.BuilderInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.panels.BuilderPanel;

@Plugin( //
		name = "Build Log Skeleton from Event Log", //
		parameterLabels = { "Event log", "Configuration" }, //
		returnLabels = { "Log Skeleton" }, //
		returnTypes = { LogSkeleton.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "http://www.win.tue.nl/~hverbeek/blog/2020/05/29/build-log-skeleton-from-event-log-3/", //
		help = "Create Log Skeleton from Event Log. Click the icon on the right for additional information." //
) //
public class BuilderPlugin extends BuilderAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Build Log Skeleton from Event Log using User Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public LogSkeleton run(UIPluginContext context, XLog log) {
		BuilderInput input = new BuilderInput(log);
		BuilderConfiguration configuration = new BuilderConfiguration(input);
		BuilderPanel panel = new BuilderPanel(log, configuration);
		InteractionResult result = context.showWizard("Configure builder (classifier)", true, true, panel);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return apply(context, input, configuration).getLogSkeleton();
	}

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Build Log Skeleton from Event Log using Default Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public LogSkeleton run(PluginContext context, XLog log) {
		BuilderInput input = new BuilderInput(log);
		BuilderConfiguration configuration = new BuilderConfiguration(input);
		return apply(context, input, configuration).getLogSkeleton();
	}

	@PluginVariant( //
			variantLabel = "Build Log Skeleton from Event Log using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public LogSkeleton run(PluginContext context, XLog log, BuilderConfiguration configuration) {
		BuilderInput input = new BuilderInput(log);
		return apply(context, input, configuration).getLogSkeleton();
	}
}
