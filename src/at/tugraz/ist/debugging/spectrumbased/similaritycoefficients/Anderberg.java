package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Anderberg extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB(), PAB() + 2.0 * (PAB() + PANB()));
	}

	@Override
	public String getCoefficientName() {
		return "Anderberg";
	}

}
