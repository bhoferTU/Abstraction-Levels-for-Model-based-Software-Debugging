package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class ExampleCounterexampleRate extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return 1.0 - div(PANB(), PAB());
	}

	@Override
	public String getCoefficientName() {
		return "Example and Counterexample Rate";
	}

}
