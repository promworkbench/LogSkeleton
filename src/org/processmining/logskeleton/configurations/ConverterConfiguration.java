package org.processmining.logskeleton.configurations;

public class ConverterConfiguration {

	private boolean interval;
	private boolean equivalence;
	private boolean always;
	private boolean exclusive;
	private boolean merge;
	
	public ConverterConfiguration() {
		setInterval(false);
		setEquivalence(false);
		setAlways(true);
		setExclusive(false);
		setMerge(true);
	}
	
	public boolean isInterval() {
		return interval;
	}
	
	public void setInterval(boolean interval) {
		this.interval = interval;
	}

	public boolean isEquivalence() {
		return equivalence;
	}

	public void setEquivalence(boolean equivalence) {
		this.equivalence = equivalence;
	}

	public boolean isAlways() {
		return always;
	}

	public void setAlways(boolean always) {
		this.always = always;
	}

	public boolean isMerge() {
		return merge;
	}

	public void setMerge(boolean merge) {
		this.merge = merge;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}
	
}
