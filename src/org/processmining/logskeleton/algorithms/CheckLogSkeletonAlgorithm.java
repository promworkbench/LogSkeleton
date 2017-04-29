package org.processmining.logskeleton.algorithms;

import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;

public class CheckLogSkeletonAlgorithm {

	public XLog apply(LogSkeleton skeleton, XLog log) {
		XLog classifiedLog = XFactoryRegistry.instance().currentDefault().createLog();
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog();
		CreateLogSkeletonAlgorithm algorithm = new CreateLogSkeletonAlgorithm();
		
		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount traceModel = algorithm.count(traceLog);
//			traceModel.print("Trace " + XConceptExtension.instance().extractName(trace));
			if (skeleton.check(trace, traceModel)) {
				classifiedLog.add(trace);
			}
		}
		return classifiedLog;
	}
}
