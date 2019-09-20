package org.processmining.logskeleton.configurations;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.logskeleton.inputs.VisualizerInput;

public class VisualizerConfiguration implements ClassifierParameter {

	private XEventClassifier classifier;

	public VisualizerConfiguration(VisualizerInput input) {
		XLog log = input.getLog();
		if (log.getClassifiers().size() > 0) {
			classifier = log.getClassifiers().get(0);
		} else {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		}
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}
}
