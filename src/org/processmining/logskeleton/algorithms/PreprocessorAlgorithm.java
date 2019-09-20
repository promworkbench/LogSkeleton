package org.processmining.logskeleton.algorithms;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.inputs.ClassifierInput;

public class PreprocessorAlgorithm {

	public ClassifierInput preprocess(PluginContext context, ClassifierInput input) {
		return input;
	}
	
	public String toString() {
		return "None";
	}
}
