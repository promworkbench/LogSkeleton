package org.processmining.logskeleton.configurations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.logskeleton.inputs.FilterBrowserInput;

public class FilterBrowserConfiguration implements ClassifierParameter {

	private XEventClassifier classifier;
	private List<List<String>> splitters;
	private Set<String> positiveFilters;
	private Set<String> negativeFilters;
	private Set<String> boundaryActivities;
	private int horizon;

	public FilterBrowserConfiguration(FilterBrowserInput input) {
		XLog log = input.getLog();
		if (log.getClassifiers().size() > 0) {
			classifier = log.getClassifiers().get(0);
		} else {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		}
		horizon = 0;
		splitters = new ArrayList<List<String>>();
		positiveFilters = new HashSet<String>();
		negativeFilters = new HashSet<String>();
		boundaryActivities = new HashSet<String>();
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

	public List<List<String>> getSplitters() {
		return splitters;
	}

	public void setSplitters(List<List<String>> splitters) {
		this.splitters = splitters;
	}

	public Set<String> getPositiveFilters() {
		return positiveFilters;
	}

	public void setPositiveFilters(Set<String> positiveFilters) {
		this.positiveFilters = positiveFilters;
	}

	public Set<String> getNegativeFilters() {
		return negativeFilters;
	}

	public void setNegativeFilters(Set<String> negativeFilters) {
		this.negativeFilters = negativeFilters;
	}

	public Set<String> getBoundaryActivities() {
		return boundaryActivities;
	}

	public void setBoundaryActivities(Set<String> boundaryActivities) {
		this.boundaryActivities = boundaryActivities;
	}
}
