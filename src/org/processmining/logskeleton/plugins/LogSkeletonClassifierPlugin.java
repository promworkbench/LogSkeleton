package org.processmining.logskeleton.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.algorithms.LogSkeletonClassifierAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;

/*
 * @deprecated Use ClassifierPlugin instead.
 */
@Deprecated
public class LogSkeletonClassifierPlugin extends LogSkeletonClassifierAlgorithm {

	public XLog run(PluginContext context, XLog trainingLog, XLog testLog) {
		return run(context, trainingLog, testLog, new LogSkeletonClassifier());
	}

	public XLog run(PluginContext context, XLog trainingLog, XLog testLog, XEventClassifier classifier) {
		return apply(context, trainingLog, testLog, classifier, new LogPreprocessorAlgorithm());
	}

}
