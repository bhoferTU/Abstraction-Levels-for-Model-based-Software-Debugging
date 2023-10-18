package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class OneWaySupport extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PB_given_A() * log2(div(PAB(), PA() * PB()));
	}

	@Override
	public String getCoefficientName() {
		return "One-Way Support";
	}

}
