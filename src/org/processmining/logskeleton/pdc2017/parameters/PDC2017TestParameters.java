package org.processmining.logskeleton.pdc2017.parameters;

import java.util.HashSet;
import java.util.Set;

import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.pdc2017.algorithms.PDC2017LogPreprocessorAlgorithm;

public class PDC2017TestParameters {

	private Set<String> logNames;
	private Set<String> allLogNames;
	private LogPreprocessorAlgorithm preprocessor;
	private Set<LogPreprocessorAlgorithm> allPreprocessors;
	private Set<String> collectionNames;
	private Set<String> allCollectionNames;
	
	public final static String CAL1 = "Calibration set 1";
	public final static String CAL2 = "Calibration set 2";
	public final static String TEST = "Test set";
	public PDC2017TestParameters() {
		
		logNames = new HashSet<String>();
		allLogNames = new HashSet<String>();
		for (int i = 1; i < 11; i++) {
			logNames.add("log"+ i);
			allLogNames.add("log"+ i);
		}
		
		preprocessor = new PDC2017LogPreprocessorAlgorithm();
		allPreprocessors = new HashSet<LogPreprocessorAlgorithm>();
		allPreprocessors.add(new LogPreprocessorAlgorithm());
		allPreprocessors.add(preprocessor);
		
		collectionNames = new HashSet<String>();
		collectionNames.add(TEST);
		allCollectionNames = new HashSet<String>();
		allCollectionNames.add(CAL1);
		allCollectionNames.add(CAL2);
		allCollectionNames.add(TEST);
	}
	
	public Set<String> getLogNames() {
		return logNames;
	}
	
	public void setLogNames(Set<String> logNames) {
		this.logNames = logNames;
	}

	public LogPreprocessorAlgorithm getPreprocessor() {
		return preprocessor;
	}

	public void setPreprocessor(LogPreprocessorAlgorithm preprocessor) {
		this.preprocessor = preprocessor;
	}

	public Set<String> getAllLogNames() {
		return allLogNames;
	}

	public void setAllLogNames(Set<String> allLogNames) {
		this.allLogNames = allLogNames;
	}

	public Set<LogPreprocessorAlgorithm> getAllPreprocessors() {
		return allPreprocessors;
	}

	public void setAllPreprocessors(Set<LogPreprocessorAlgorithm> allPreprocessors) {
		this.allPreprocessors = allPreprocessors;
	}

	public Set<String> getCollectionNames() {
		return collectionNames;
	}

	public void setCollectionNames(Set<String> collectionNames) {
		this.collectionNames = collectionNames;
	}

	public Set<String> getAllCollectionNames() {
		return allCollectionNames;
	}

	public void setAllCollectionNames(Set<String> allCollectionNames) {
		this.allCollectionNames = allCollectionNames;
	}
	
}
