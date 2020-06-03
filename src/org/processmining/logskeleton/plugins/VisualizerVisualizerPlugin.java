package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.VisualizerAlgorithm;
import org.processmining.logskeleton.components.VisualizerComponent;
import org.processmining.logskeleton.configurations.VisualizerConfiguration;
import org.processmining.logskeleton.inputs.VisualizerInput;

@Plugin( //
		name = "Visualize Log as Log Skeleton", //
		parameterLabels = { "Event Log", "Configuration", "Visualizer Component" }, //
		returnLabels = { "Log Skeleton Visualizer" }, //
		returnTypes = { VisualizerComponent.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "http://www.win.tue.nl/~hverbeek/blog/2020/06/02/visualize-log-as-log-skeleton-7/", //
		help = "Log Skeleton Visualizer" //
) //
@Visualizer
public class VisualizerVisualizerPlugin extends VisualizerAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Visualize Log as Log Skeleton using Default Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public VisualizerComponent run(UIPluginContext context, XLog log) {
		VisualizerInput input = new VisualizerInput(log);
		VisualizerConfiguration configuration = new VisualizerConfiguration(input);
		return apply(context, input, configuration).getComponent();
	}
	
	@PluginVariant( //
			variantLabel = "Visualize Log as Log Skeleton using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public VisualizerComponent run(PluginContext context, XLog log, VisualizerConfiguration configuration) {
		VisualizerInput input = new VisualizerInput(log);
		return apply(context, input, configuration).getComponent();
	}
	
	@PluginVariant( //
			variantLabel = "Visualize Log as Log Skeleton using Visualizer Component", //
			requiredParameterLabels = { 2 } //
	) //
	public VisualizerComponent run(PluginContext context, VisualizerComponent component) {
		return component;
	}
}
