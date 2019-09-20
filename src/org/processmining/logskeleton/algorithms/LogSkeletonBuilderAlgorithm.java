package org.processmining.logskeleton.algorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.configurations.BuilderConfiguration;
import org.processmining.logskeleton.inputs.BuilderInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;

/*
 * @deprecated Use BuilderAlgorithm instead.
 */
@Deprecated
public class LogSkeletonBuilderAlgorithm extends BuilderAlgorithm {

	public LogSkeleton apply(PluginContext context, XLog log, XEventClassifier classifier) {
		BuilderInput input = new BuilderInput(log);
		BuilderConfiguration configuration = new BuilderConfiguration(input);
		configuration.setClassifier(classifier);
		return super.apply(context, input, configuration).getLogSkeleton();
	}

	public LogSkeletonCount count(XLog log, XEventClassifier classifier) {
		BuilderInput input = new BuilderInput(log);
		BuilderConfiguration configuration = new BuilderConfiguration(input);
		configuration.setClassifier(classifier);
		return super.count(log, configuration);
	}
}
