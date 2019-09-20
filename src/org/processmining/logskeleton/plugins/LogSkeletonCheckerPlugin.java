package org.processmining.logskeleton.plugins;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.algorithms.LogSkeletonCheckerAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.models.LogSkeleton;

/*
 * @deprecated Use CheckerPlugin instead.
 */
@Deprecated
public class LogSkeletonCheckerPlugin extends LogSkeletonCheckerAlgorithm {

	public XLog run(PluginContext context, LogSkeleton model, XLog log) {
		return run(context, model, log, new LogSkeletonClassifier());
	}

	public XLog run(PluginContext context, LogSkeleton model, XLog log, XEventClassifier classifier) {
		boolean[] checks = new boolean[] { true, true, true };
		return run(context, model, log, classifier, new HashSet<String>(), checks);
	}

	public XLog run(PluginContext context, LogSkeleton model, XLog log, Set<String> messages, boolean[] checks) {
		XEventClassifier classifier = new LogSkeletonClassifier();
		return run(context, model, log, classifier, messages, checks);
	}

	public XLog run(PluginContext context, LogSkeleton model, XLog log, XEventClassifier classifier, Set<String> messages, boolean[] checks) {
		return apply(model, log, classifier, messages, checks);
	}
}
