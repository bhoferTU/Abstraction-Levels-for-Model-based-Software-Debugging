package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class YulesY extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = Math.sqrt(PAB() * PNANB());
		double term2 = Math.sqrt(PANB() * PNAB());
		return div(term1 - term2, term1 + term2);
	}

	@Override
	public String getCoefficientName() {
		return "Yule's Y";
	}

}
