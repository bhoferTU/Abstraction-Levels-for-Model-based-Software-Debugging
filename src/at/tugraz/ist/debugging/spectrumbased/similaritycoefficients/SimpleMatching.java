package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class SimpleMatching extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PAB() + PNANB();
	}

	@Override
	public String getCoefficientName() {
		return "Simple-Matching";
	}

}
