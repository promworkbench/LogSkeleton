package org.processmining.logskeleton.configurations;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.logskeleton.inputs.BuilderInput;

public class BuilderConfiguration implements ClassifierParameter {

	private XEventClassifier classifier;
	private int horizon;
	private Set<String> boundaryActivities;

	public BuilderConfiguration(BuilderInput input) {
		XLog log = input.getLog();
		if (log.getClassifiers().size() > 0) {
			classifier = log.getClassifiers().get(0);
		} else {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		}
		horizon = 0;
		boundaryActivities = new HashSet<String>();
	}
	
	public BuilderConfiguration(CheckerConfiguration configuration) {
		setClassifier(configuration.getClassifier());
	}
	
	public BuilderConfiguration(ClassifierConfiguration configuration) {
		setClassifier(configuration.getClassifier());
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public int getHorizon() {
		return horizon;
	}

	public void setHorizon(int horizon) {
		this.horizon = horizon;
	}

	public Set<String> getBoundaryActivities() {
		return boundaryActivities;
	}

	public void setBoundaryActivities(Set<String> boundaryActivities) {
		this.boundaryActivities = boundaryActivities;
	}
}
