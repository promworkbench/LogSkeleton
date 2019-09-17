package org.processmining.logskeleton.configurations;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;

public class LogPartitionerConfiguration implements ClassifierParameter {

	private LogSkeletonClassifier classifier;

	public LogPartitionerConfiguration(XLog log) {
		if (log.getClassifiers().size() > 0) {
			classifier = new LogSkeletonClassifier(log.getClassifiers().get(0));
		} else {
			classifier = new LogSkeletonClassifier();
		}
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		if (classifier instanceof LogSkeletonClassifier) {
			this.classifier = (LogSkeletonClassifier) classifier;
		} else {
			this.classifier = new LogSkeletonClassifier(classifier);
		}
	}
}
