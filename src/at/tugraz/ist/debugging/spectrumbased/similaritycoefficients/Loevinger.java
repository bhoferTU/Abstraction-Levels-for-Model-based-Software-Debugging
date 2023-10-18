package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Loevinger extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return 1.0 - div(PA() * (1.0 - PB()), PANB());
	}

	@Override
	public String getCoefficientName() {
		return "Loevinger";
	}

}
