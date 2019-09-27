package org.processmining.logskeleton.algorithms;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.configurations.BuilderConfiguration;
import org.processmining.logskeleton.configurations.CheckerConfiguration;
import org.processmining.logskeleton.inputs.CheckerInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.outputs.CheckerOutput;

public class CheckerAlgorithm {

	public CheckerOutput apply(PluginContext context, CheckerInput input, CheckerConfiguration configuration) {
		XLog classifiedLog = XFactoryRegistry.instance().currentDefault().createLog();
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog();
		BuilderAlgorithm algorithm = new BuilderAlgorithm();
		BuilderConfiguration builderConfiguration = new BuilderConfiguration(configuration);
		
		XLog log = input.getLog();
		LogSkeleton logSkeleton = input.getLogSkeleton();
		
		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount traceCount = algorithm.count(traceLog, builderConfiguration);
			traceCount.print("Trace " + XConceptExtension.instance().extractName(trace));
			if (logSkeleton.check(trace, traceCount, configuration).isEmpty()) {
				classifiedLog.add(trace);
			}
		}
		return new CheckerOutput(classifiedLog);
	}
}
