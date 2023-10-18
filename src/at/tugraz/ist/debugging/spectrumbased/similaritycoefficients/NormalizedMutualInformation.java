package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class NormalizedMutualInformation extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = calculateLog(PAB(), PA(), PB())
				+ calculateLog(PNAB(), 1.0 - PA(), PB())
				+ calculateLog(PANB(), PA(), 1.0 - PB())
				+ calculateLog(PNANB(), 1.0 - PA(), 1.0 - PB());
		return div(term1,
				-(PA() * log2(PA()) + (1.0 - PA()) * log2(1.0 - PA())));
	}

	private double calculateLog(double term1, double term2, double term3) {
		return term1 * log2(div(term1, term2 * term3));
	}

	@Override
	public String getCoefficientName() {
		return "Normalized Mutual Information";
	}

}
