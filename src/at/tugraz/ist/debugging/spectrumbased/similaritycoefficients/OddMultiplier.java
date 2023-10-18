package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class OddMultiplier extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() * (1.0 - PB()), PB() * PANB());
	}

	@Override
	public String getCoefficientName() {
		return "Odd Multiplier";
	}

}
