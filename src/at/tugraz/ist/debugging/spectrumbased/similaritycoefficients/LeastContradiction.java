package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class LeastContradiction extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() - PANB(), PB());
	}

	@Override
	public String getCoefficientName() {
		return "Least Contradiction";
	}

}
