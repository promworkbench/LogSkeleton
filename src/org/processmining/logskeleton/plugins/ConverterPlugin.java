package org.processmining.logskeleton.plugins;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.ConverterAlgorithm;
import org.processmining.logskeleton.algorithms.GraphBuilderAlgorithm;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.configurations.ConverterConfiguration;
import org.processmining.logskeleton.inputs.BrowserInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

@Plugin( //
		name = "Convert Log Skeleton to Petri Net", //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/", //
		parameterLabels = { "Log Skeleton", "Configuration" }, //
		returnLabels = { "Petri Net" }, //
		returnTypes = { Petrinet.class }, //
		userAccessible = true, //
		help = "Convert Log Skeleton to Petri Net." //
) //
public class ConverterPlugin extends ConverterAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Convert Log Skeleton to Petri Net using Default Configuration", //
			requiredParameterLabels = { 0 } //
	) //
	public Petrinet run(PluginContext context, LogSkeleton skeleton) {
		GraphBuilderAlgorithm builder = new GraphBuilderAlgorithm();
		LogSkeletonGraph graph = builder.apply(skeleton, new BrowserConfiguration(new BrowserInput(skeleton)));
		return apply(context, graph, new ConverterConfiguration());
	}
	
	@PluginVariant( //
			variantLabel = "Convert Log Skeleton to Petri Net using Provided Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public Petrinet run(PluginContext context, LogSkeleton skeleton, ConverterConfiguration configuration) {
		GraphBuilderAlgorithm builder = new GraphBuilderAlgorithm();
		LogSkeletonGraph graph = builder.apply(skeleton, new BrowserConfiguration(new BrowserInput(skeleton)));
		return apply(context, graph, configuration);
	}
	
	
}
