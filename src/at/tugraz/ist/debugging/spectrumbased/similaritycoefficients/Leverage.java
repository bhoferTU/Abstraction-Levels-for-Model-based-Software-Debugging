package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Leverage extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PB_given_A() - PA() * PB();
	}

	@Override
	public String getCoefficientName() {
		return "Leverage";
	}

}
