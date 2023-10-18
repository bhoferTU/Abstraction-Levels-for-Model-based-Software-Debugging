package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Conviction extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = div(PA() * (1 - PB()), PANB());
		double term2 = div(PB() * (1 - PA()), PANB());

		return Math.max(term1, term2);
	}

	@Override
	public String getCoefficientName() {
		return "Conviction";
	}

}
