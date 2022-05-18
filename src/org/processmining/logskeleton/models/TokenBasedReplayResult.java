package org.processmining.logskeleton.models;

public class TokenBasedReplayResult {

	private int missing;
	private int consumed;
	private int remaining;
	private int produced;

	public TokenBasedReplayResult() {
		missing = 0;
		consumed = 0;
		remaining = 0;
		produced = 0;
	}

	public void addMissing(int n) {
		if (n > 0) {
			missing += n;
		}
	}

	public void addConsumed(int n) {
		if (n > 0) {
			consumed += n;
		}
	}

	public void addRemaining(int n) {
		if (n > 0) {
			remaining += n;
		}
	}

	public void addProduced(int n) {
		if (n > 0) {
			produced += n;
		}
	}

	public boolean hasPerfectFitness() {
		return missing == 0 && remaining == 0;
	}

	public double getFitness() {
		return 0.5 * ((consumed - missing) / (1.0 * consumed)) + 0.5 * ((produced - remaining) / (1.0 * produced));
	}
	
	public String toString() {
		return "consumed(missing)=" + consumed + "(" + missing + "), produced(remaining)=" + produced + "(" + remaining +")";
	}
}
