package org.processmining.logskeleton.configurations;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.logskeleton.algorithms.PreprocessorAlgorithm;
import org.processmining.logskeleton.inputs.ClassifierInput;

public class ClassifierConfiguration implements ClassifierParameter {

	private XEventClassifier classifier;
	private PreprocessorAlgorithm preprocessor;
	private boolean createProvidedObjects;
	private int maxFilterDepth;

	public ClassifierConfiguration(ClassifierInput input) {
		XLog log = input.getReferenceLog();
		if (log.getClassifiers().size() > 0) {
			classifier = log.getClassifiers().get(0);
		} else {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		}
		preprocessor = new PreprocessorAlgorithm();
		createProvidedObjects = true;
		maxFilterDepth = 2;
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public PreprocessorAlgorithm getPreprocessor() {
		return preprocessor;
	}

	public void setPreprocessor(PreprocessorAlgorithm preprocessor) {
		this.preprocessor = preprocessor;
	}

	public boolean isCreateProvidedObjects() {
		return createProvidedObjects;
	}

	public void setCreateProvidedObjects(boolean createProvidedObjects) {
		this.createProvidedObjects = createProvidedObjects;
	}

	public int getMaxFilterDepth() {
		return maxFilterDepth;
	}

	public void setMaxFilterDepth(int maxFilterDepth) {
		this.maxFilterDepth = maxFilterDepth;
	}
}
