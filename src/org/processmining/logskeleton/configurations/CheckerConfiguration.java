package org.processmining.logskeleton.configurations;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.logskeleton.inputs.CheckerInput;

public class CheckerConfiguration implements ClassifierParameter {

	private XEventClassifier classifier;
	private boolean[] checks;
	private boolean stopAtFirstViolation;
	
	public CheckerConfiguration(CheckerInput input) {
		XLog log = input.getLog();
		if (log.getClassifiers().size() > 0) {
			classifier = log.getClassifiers().get(0);
		} else {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		}
		checks = new boolean[] { true, true, true };
		stopAtFirstViolation = false;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}
	
	public boolean[] getChecks() {
		return checks;
	}
	
	public void setCheck(int i, boolean b) {
		if (i >= 0 && i < 3) {
			checks[i] = b;
		}
	}

	public boolean isStopAtFirstViolation() {
		return stopAtFirstViolation;
	}

	public void setStopAtFirstViolation(boolean stopAtFirstViolation) {
		this.stopAtFirstViolation = stopAtFirstViolation;
	}
}
