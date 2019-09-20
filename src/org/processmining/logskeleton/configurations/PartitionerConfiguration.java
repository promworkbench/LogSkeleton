package org.processmining.logskeleton.configurations;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.logskeleton.inputs.PartitionerInput;

public class PartitionerConfiguration implements ClassifierParameter {

	private XEventClassifier classifier;
	private boolean createProvidedObjects;

	public PartitionerConfiguration(PartitionerInput input) {
		XLog log = input.getLog();
		if (log.getClassifiers().size() > 0) {
			classifier = log.getClassifiers().get(0);
		} else {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		}
		setCreateProvidedObjects(false);
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public boolean isCreateProvidedObjects() {
		return createProvidedObjects;
	}

	public void setCreateProvidedObjects(boolean createProvidedObjects) {
		this.createProvidedObjects = createProvidedObjects;
	}
}
