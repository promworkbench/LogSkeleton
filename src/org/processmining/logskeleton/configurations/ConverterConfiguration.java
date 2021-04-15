package org.processmining.logskeleton.configurations;

public class ConverterConfiguration {

	private boolean interval;
	private boolean equivalence;
	private boolean alwaysAfter;
	private boolean alwaysBefore;
	private boolean never;
	private boolean exclusive;
	private boolean merge;
	private boolean marking;
	private boolean skip;
	
	public ConverterConfiguration() {
		setInterval(true);
		setEquivalence(true);
		setAlwaysAfter(true);
		setAlwaysBefore(true);
		setNever(true);
		setExclusive(true);
		setMerge(true);
		setMarking(true);
		setSkip(false);
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

	public boolean isMarking() {
		return marking;
	}

	public void setMarking(boolean marking) {
		this.marking = marking;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public boolean isAlwaysAfter() {
		return alwaysAfter;
	}

	public void setAlwaysAfter(boolean alwaysAfter) {
		this.alwaysAfter = alwaysAfter;
	}

	public boolean isAlwaysBefore() {
		return alwaysBefore;
	}

	public void setAlwaysBefore(boolean alwaysBefore) {
		this.alwaysBefore = alwaysBefore;
	}

	public boolean isNever() {
		return never;
	}

	public void setNever(boolean never) {
		this.never = never;
	}
	
}
