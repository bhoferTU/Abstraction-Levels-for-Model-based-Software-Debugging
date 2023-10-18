package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class InterestingnessWeightingDependency extends SimilarityCoefficient {

	private static double k = 1.0;
	private static double m = 1.0;

	@Override
	protected Double calculateCoefficient() {
		return (Math.pow(div(PAB(), PA() * PB()), k) - 1.0)
				* Math.pow(PAB(), m);
	}

	@Override
	public String getCoefficientName() {
		return "Interestingness Weighting Dependency";
	}

}
