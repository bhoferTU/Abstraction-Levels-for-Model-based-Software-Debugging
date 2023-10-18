package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class JMeasure extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = PAB() * log(div(PB_given_A(), PB())) + PANB()
				* log(div(1.0 - PB_given_A(), 1.0 - PB()));
		double term2 = PAB() * log(div(PB_given_A(), PA())) + PANB()
				* log(div(1.0 - PB_given_A(), 1.0 - PA()));
		return Math.max(term1, term2);
	}

	@Override
	public String getCoefficientName() {
		return "J-Measure";
	}

}
