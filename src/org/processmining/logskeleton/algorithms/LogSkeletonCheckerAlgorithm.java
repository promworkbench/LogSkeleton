package org.processmining.logskeleton.algorithms;

import java.util.Collection;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.configurations.CheckerConfiguration;
import org.processmining.logskeleton.inputs.CheckerInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.models.Violation;

/*
 * deprecated Use CheckerAlgoirhtm instead
 */
@Deprecated
public class LogSkeletonCheckerAlgorithm {

	public XLog apply(LogSkeleton skeleton, XLog log, XEventClassifier classifier, Set<String> messages, boolean[] checks) {
		XLog classifiedLog = XFactoryRegistry.instance().currentDefault().createLog();
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog();
		LogSkeletonBuilderAlgorithm algorithm = new LogSkeletonBuilderAlgorithm();
		CheckerConfiguration configuration = new CheckerConfiguration(new CheckerInput(skeleton, log));
		configuration.setClassifier(classifier);
		for (int i = 0; i < 3; i++) {
			configuration.setCheck(i, checks[i]);
		}
		
		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount traceModel = algorithm.count(traceLog, classifier);
			traceModel.print("Trace " + XConceptExtension.instance().extractName(trace));
			Collection<Violation> violations = skeleton.check(trace, traceModel, configuration);
			if (violations.isEmpty()) {
				classifiedLog.add(trace);
			}
			for (Violation violation : violations) {
				messages.add(violation.toString());
			}
		}
		return classifiedLog;
	}
}
