package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class YulesQ extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = PAB() * PNANB();
		double term2 = PANB() * PNAB();
		return div(term1 - term2, term1 + term2);
	}

	@Override
	public String getCoefficientName() {
		return "Yule's Q";
	}

}
