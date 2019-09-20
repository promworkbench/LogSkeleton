package org.processmining.logskeleton.classifiers;

import org.deckfour.xes.classification.XEventClassifier;

/*
 * @deprecated Use PrefixClassifier instead.
 */
@Deprecated
public class LogSkeletonClassifier extends PrefixClassifier {

	public LogSkeletonClassifier() {
		super();
	}
	
	public LogSkeletonClassifier(XEventClassifier classifier) {
		super(classifier);
	}
}
