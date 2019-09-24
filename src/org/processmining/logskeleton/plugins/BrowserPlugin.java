package org.processmining.logskeleton.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.BrowserAlgorithm;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.inputs.BrowserInput;
import org.processmining.logskeleton.models.LogSkeleton;

@Plugin( //
		name = "Log Skeleton Browser", //
		parameterLabels = { "Log Skeleton", "Configuration" }, //
		returnLabels = { "Log Skeleton Browser" }, //
		returnTypes = {	JComponent.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/blog/2019/09/24/log-skeleton-browser-2/", //
		help = "Log Skeleton Browser" //
) //
@Visualizer //
public class BrowserPlugin extends BrowserAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Log Skeleton Browser using Default Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public JComponent run(PluginContext context, LogSkeleton logSkeleton) {
		BrowserInput input = new BrowserInput(logSkeleton);
		BrowserConfiguration configuration = new BrowserConfiguration(input);
		return apply(context, input, configuration).getComponent();
	}

	@PluginVariant( //
			variantLabel = "Log Skeleton Browser using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public JComponent run(PluginContext context, LogSkeleton logSkeleton, BrowserConfiguration configuration) {
		BrowserInput input = new BrowserInput(logSkeleton);
		return apply(context, input, configuration).getComponent();
	}
}
