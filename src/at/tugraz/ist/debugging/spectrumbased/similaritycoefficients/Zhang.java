package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Zhang extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() - PA() * PB(),
				Math.max(PAB() * (1.0 - PB()), PB() * PANB()));
	}

	@Override
	public String getCoefficientName() {
		return "Zhang";
	}

}
