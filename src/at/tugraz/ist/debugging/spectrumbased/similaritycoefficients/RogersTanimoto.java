package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class RogersTanimoto extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() + PNANB(), PAB() + PNANB() + 2.0 * (PNAB() + PANB()));
	}

	@Override
	public String getCoefficientName() {
		return "Rogers and Tanimoto";
	}

}
