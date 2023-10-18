package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Coverage extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PA();
	}

	@Override
	public String getCoefficientName() {
		return "Coverage";
	}

}
