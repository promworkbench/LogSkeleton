package org.processmining.logskeleton.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.algorithms.LogSkeletonBuilderAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.models.LogSkeleton;

/*
 * @deprecated Use BuilderPlugin instead.
 */
@Deprecated
public class LogSkeletonBuilderPlugin extends LogSkeletonBuilderAlgorithm  {

	public LogSkeleton run(PluginContext context, XLog log) {
		return apply(context, log, new LogSkeletonClassifier());
	}

	public LogSkeleton run(PluginContext context, XLog log, XEventClassifier classifier) {
		return apply(context, log, classifier);
	}
}
