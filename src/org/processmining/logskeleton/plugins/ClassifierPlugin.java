package org.processmining.logskeleton.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.ClassifierAlgorithm;
import org.processmining.logskeleton.configurations.ClassifierConfiguration;
import org.processmining.logskeleton.inputs.ClassifierInput;
import org.processmining.logskeleton.panels.ClassifierPanel;

@Plugin( //
		name = "Classify Log using Log Skeleton", //
		icon = "prom_duck.png", //
		url = "https://www.win.tue.nl/~hverbeek/blog/2019/09/20/classify-log-using-log-skeleton/", //
		parameterLabels = { "Reference log", "Log", "Configuration" }, //
		returnLabels = { "Classified Log" }, //
		returnTypes = { XLog.class }, //
		userAccessible = true, //
		help = "Classify Log using Log Skeleton" //
) //
public class ClassifierPlugin extends ClassifierAlgorithm {

	@UITopiaVariant( //
			affiliation = UITopiaVariant.EHV, //
			author = "H.M.W. Verbeek", //
			email = "h.m.w.verbeek@tue.nl" //
	) //
	@PluginVariant( //
			variantLabel = "Classify Log using Log Skeleton using User Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public XLog run(UIPluginContext context, XLog referenceLog, XLog log) {
		ClassifierInput input = new ClassifierInput(referenceLog, log);
		ClassifierConfiguration configuration = new ClassifierConfiguration(input);
		ClassifierPanel panel = new ClassifierPanel(referenceLog, configuration);
		InteractionResult result = context.showWizard("Configure classifier (classifier, options)", true, true, panel);
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
			variantLabel = "Classify Log using Log Skeleton using Default Configuration", //
			requiredParameterLabels = { 0, 1 } //
	) //
	public XLog run(PluginContext context, XLog referenceLog, XLog log) {
		ClassifierInput input = new ClassifierInput(referenceLog, log);
		ClassifierConfiguration configuration = new ClassifierConfiguration(input);
		return apply(context, input, configuration).getLog();
	}

	@PluginVariant( //
			variantLabel = "Classify Log using Log Skeleton using Provided Configuration", //
			requiredParameterLabels = { 0, 1, 2 } //
	) //
	public XLog run(PluginContext context, XLog referenceLog, XLog log, ClassifierConfiguration configuration) {
		ClassifierInput input = new ClassifierInput(referenceLog, log);
		return apply(context, input, configuration).getLog();
	}

}
