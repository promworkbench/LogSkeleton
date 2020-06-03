package org.processmining.logskeleton.plugins;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.BrowserAlgorithm;
import org.processmining.logskeleton.components.BrowserComponent;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.inputs.BrowserInput;
import org.processmining.logskeleton.models.LogSkeleton;

@Plugin( //
		name = "Log Skeleton Browser", //
		parameterLabels = { "Log Skeleton", "Configuration", "Browser Component" }, //
		returnLabels = { "Log Skeleton Browser" }, //
		returnTypes = { BrowserComponent.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "http://www.win.tue.nl/~hverbeek/blog/2020/06/02/log-skeleton-browser-7/", //
		help = "Log Skeleton Browser" //
) //
@Visualizer //
public class BrowserVisualizerPlugin extends BrowserAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Log Skeleton Browser using Default Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public BrowserComponent run(PluginContext context, LogSkeleton logSkeleton) {
		BrowserInput input = new BrowserInput(logSkeleton);
		BrowserConfiguration configuration = new BrowserConfiguration(input);
		return apply(context, input, configuration).getComponent();
	}

	@PluginVariant( //
			variantLabel = "Log Skeleton Browser using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public BrowserComponent run(PluginContext context, LogSkeleton logSkeleton, BrowserConfiguration configuration) {
		BrowserInput input = new BrowserInput(logSkeleton);
		return apply(context, input, configuration).getComponent();
	}

	@PluginVariant( //
			variantLabel = "Log Skeleton Browser using Browser Component", //
			requiredParameterLabels = { 2 } //
	) //
	public BrowserComponent run(PluginContext context, BrowserComponent component) {
		return component;
	}
}
