package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class TwoWaySupport extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PAB() * log2(div(PAB(), PA() * PB()));
	}

	@Override
	public String getCoefficientName() {
		return "Two-Way Support";
	}

}
