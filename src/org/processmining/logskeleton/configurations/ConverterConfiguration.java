package org.processmining.logskeleton.configurations;

public class ConverterConfiguration {

	private boolean merge;
	
	private boolean marking;
	
	private boolean interval;
	private boolean equivalence;
	private boolean alwaysAfter;
	private boolean alwaysBefore;
	private boolean never;
	private boolean exclusive;
	
	private boolean optimizeEquivalence;
	private boolean optimizeAlwaysNever;
	private boolean optimizeAlwaysEquivalence;
	private boolean optimizeAlwaysElementary;
	private boolean optimizeNeverEquivalence;
	private boolean optimizeNeverAlways;
	private boolean optimizeNeverElementary;
	
	public ConverterConfiguration() {
		/*
		 * Whether to merge activity transitions (typically set to true).
		 */
		setMerge(true); 
		
		/*
		 * Whether to use initial and final marking instead of arcs from |> and to [].
		 */
		setMarking(true); 

		/*
		 * Set which constraints to convert.
		 */
		setInterval(false);
		setEquivalence(true);
		setAlwaysAfter(true);
		setAlwaysBefore(true);
		setNever(true);
		setExclusive(true);
	
		/*
		 * Set which optimizations to use. 
		 */
		setOptimizeEquivalence(true);
		setOptimizeAlwaysNever(true);
		setOptimizeAlwaysEquivalence(true);
		setOptimizeAlwaysElementary(true);
		setOptimizeNeverEquivalence(true);
		setOptimizeNeverAlways(true);
		setOptimizeNeverElementary(true);
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

	public boolean isOptimizeEquivalence() {
		return optimizeEquivalence;
	}

	public void setOptimizeEquivalence(boolean optimizeEquivalence) {
		this.optimizeEquivalence = optimizeEquivalence;
	}

	public boolean isOptimizeAlwaysNever() {
		return optimizeAlwaysNever;
	}

	public void setOptimizeAlwaysNever(boolean optimizeAlwaysNever) {
		this.optimizeAlwaysNever = optimizeAlwaysNever;
	}

	public boolean isOptimizeAlwaysEquivalence() {
		return optimizeAlwaysEquivalence;
	}

	public void setOptimizeAlwaysEquivalence(boolean optimizeAlwaysEquivalence) {
		this.optimizeAlwaysEquivalence = optimizeAlwaysEquivalence;
	}

	public boolean isOptimizeAlwaysElementary() {
		return optimizeAlwaysElementary;
	}

	public void setOptimizeAlwaysElementary(boolean optimizeAlwaysElementary) {
		this.optimizeAlwaysElementary = optimizeAlwaysElementary;
	}

	public boolean isOptimizeNeverEquivalence() {
		return optimizeNeverEquivalence;
	}

	public void setOptimizeNeverEquivalence(boolean optimizeNeverEquivalence) {
		this.optimizeNeverEquivalence = optimizeNeverEquivalence;
	}

	public boolean isOptimizeNeverAlways() {
		return optimizeNeverAlways;
	}

	public void setOptimizeNeverAlways(boolean optimizeNeverAlways) {
		this.optimizeNeverAlways = optimizeNeverAlways;
	}

	public boolean isOptimizeNeverElementary() {
		return optimizeNeverElementary;
	}

	public void setOptimizeNeverElementary(boolean optimizeNeverElementary) {
		this.optimizeNeverElementary = optimizeNeverElementary;
	}
	
}
