package org.processmining.logskeleton.plugins;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.VisualizerAlgorithm;
import org.processmining.logskeleton.configurations.VisualizerConfiguration;
import org.processmining.logskeleton.inputs.VisualizerInput;

@Plugin( //
		name = "Visualize Log as Log Skeleton", //
		parameterLabels = { "Event Log", "Configuration" }, //
		returnLabels = { "Log Skeleton Visualizer" }, //
		returnTypes = { JComponent.class }, //
		userAccessible = true, //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/blog/2019/09/20/visualize-log-as-log-skeleton/", //
		help = "Log Skeleton Visualizer" //
) //
@Visualizer
public class VisualizerPlugin extends VisualizerAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Visualize Log as Log Skeleton using Default Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public JComponent run(UIPluginContext context, XLog log) {
		VisualizerInput input = new VisualizerInput(log);
		VisualizerConfiguration configuration = new VisualizerConfiguration(input);
		return apply(context, input, configuration).getComponent();
	}
	
	@PluginVariant( //
			variantLabel = "Visualize Log as Log Skeleton using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public JComponent run(PluginContext context, XLog log, VisualizerConfiguration configuration) {
		VisualizerInput input = new VisualizerInput(log);
		return apply(context, input, configuration).getComponent();
	}
	
}
