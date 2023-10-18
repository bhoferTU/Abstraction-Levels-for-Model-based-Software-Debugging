package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

import java.util.Comparator;

/**
 * Abstract class for similarity coefficient computation.
 * 
 * @author bhofer
 * 
 */
public abstract class SimilarityCoefficient {

	public static class CoefficientComparator implements
			Comparator<SimilarityCoefficient> {
		@Override
		public int compare(SimilarityCoefficient o1, SimilarityCoefficient o2) {
			return o1.getCoefficientName().compareTo(o2.getCoefficientName());
		}

	}

	protected static double div(double term1, double term2) {
		if (term2 != 0)
			return term1 / term2;
		else
			return 0.0;
	}

	/**
	 * Creates a new object of the state of the art coefficient ({@link Ochiai})
	 * and returns it.
	 * 
	 * @return Reference to a new Object of the state of the art coefficient (
	 *         {@link Ochiai})
	 */
	public static SimilarityCoefficient getDefaultSimilarityCoefficient() {
		return new Ochiai();
	}

	protected static double log(double term) {
		if (term != 0) {
			return Math.log(term);
		} else
			return 0.0;
	}

	protected static double log2(double term) {
		if (term != 0) {
			return Math.log(term) / Math.log(2);
		} else
			return 0.0;
	}

	protected int a11, a10, a01, a00;

	protected int total;

	protected abstract Double calculateCoefficient();

	/**
	 * Returns the name of the created coeffient object, e.g. "Ochiai"
	 * 
	 * @return Name of the used coefficient, e.g. "Ochiai"
	 */
	public abstract String getCoefficientName();

	/**
	 * Computes the similarity coefficient for the given hit spectra information
	 * 
	 * @param a11
	 *            Number of failed and involved test cases
	 * @param a10
	 *            Number of passed and involved test cases
	 * @param a01
	 *            Number of failed and not involved test cases
	 * @param a00
	 *            Number of passed and not involved test cases
	 * @return Computed similarity coefficient
	 */
	public Double getSimilarityCoefficient(int a11, int a10, int a01, int a00) {
		this.a11 = a11;
		this.a10 = a10;
		this.a01 = a01;
		this.a00 = a00;
		this.total = a11 + a10 + a01 + a00;

		return calculateCoefficient();
	}

	protected double PA() {
		return div(a11 + a10, total);
	}

	protected double PA_given_B() {
		return div(PAB(), PB());
	}

	protected double PA_given_NB() {
		return div(PANB(), 1.0 - PB());
	}

	protected double PAB() {
		return div(a11, total);
	}

	protected double PANB() {
		return div(a10, total);
	}

	protected double PB() {
		return div(a01 + a11, total);
	}

	protected double PB_given_A() {
		return div(PAB(), PA());
	}

	protected double PB_given_NA() {
		return div(PNAB(), 1.0 - PA());
	}

	protected double PNAB() {
		return div(a01, total);
	}

	protected double PNANB() {
		return div(a00, total);
	}
}
