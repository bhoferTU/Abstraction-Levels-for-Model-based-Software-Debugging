package at.tugraz.ist.debugging.spreadsheets.evaluation.ranking;

import java.util.HashSet;

public class RankEntry<K> extends HashSet<K> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	double value;

	public RankEntry(Double value, K key) {
		this.value = value;
		add(key);
	}

	public double getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "[" + value + "] (" + size() + ") " + super.toString();
	}

}
