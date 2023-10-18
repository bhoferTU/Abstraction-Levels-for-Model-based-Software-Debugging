package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class PiatetskyShapiro extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PAB() - PA() * PB();
	}

	@Override
	public String getCoefficientName() {
		return "Piatetsky-Shapiro";
	}

}
