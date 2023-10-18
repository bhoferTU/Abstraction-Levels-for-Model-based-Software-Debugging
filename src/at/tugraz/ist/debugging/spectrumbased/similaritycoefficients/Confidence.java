package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Confidence extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return Math.max(PB_given_A(), PA_given_B());
	}

	@Override
	public String getCoefficientName() {
		return "Confidence";
	}

}
