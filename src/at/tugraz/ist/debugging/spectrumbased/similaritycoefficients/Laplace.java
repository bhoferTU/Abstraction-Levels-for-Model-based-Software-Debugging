package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class Laplace extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = div(PAB() + 1.0, PA() + 2.0);
		double term2 = div(PAB() + 1.0, PB() + 2.0);
		return Math.max(term1, term2);
	}

	@Override
	public String getCoefficientName() {
		return "Laplace";
	}

}
