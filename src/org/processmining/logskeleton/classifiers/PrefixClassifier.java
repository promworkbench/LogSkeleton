package org.processmining.logskeleton.classifiers;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XVisitor;

public class PrefixClassifier implements XEventClassifier {

	private XEventClassifier prefixClassifier;
	public final static String SUFFIX = ".suffix";
	
	public PrefixClassifier() {
		this(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
	}
	
	public PrefixClassifier(XEventClassifier classifier) {
		if (classifier instanceof PrefixClassifier) {
			this.prefixClassifier = ((PrefixClassifier) classifier).prefixClassifier;
		} else {
			this.prefixClassifier = classifier;
		}
	}
	
	public void accept(XVisitor arg0, XLog arg1) {
	}

	public String getClassIdentity(XEvent event) {
		if (event.getAttributes().containsKey(SUFFIX)) {
			return prefixClassifier.getClassIdentity(event) + event.getAttributes().get(SUFFIX).toString();
		}
		return prefixClassifier.getClassIdentity(event);
	}

	public String[] getDefiningAttributeKeys() {
		return prefixClassifier.getDefiningAttributeKeys();
	}

	public String name() {
		return null;
	}

	public boolean sameEventClass(XEvent arg0, XEvent arg1) {
		return false;
	}

	public void setName(String arg0) {
	}
}
