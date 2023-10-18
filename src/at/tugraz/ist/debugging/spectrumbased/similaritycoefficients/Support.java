package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Support extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PAB();
	}

	@Override
	public String getCoefficientName() {
		return "Support";
	}

}
