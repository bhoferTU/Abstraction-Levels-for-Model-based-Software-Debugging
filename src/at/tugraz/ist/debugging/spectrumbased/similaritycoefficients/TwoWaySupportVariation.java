package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class TwoWaySupportVariation extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return PAB() * log2(div(PAB(), PA() * PB())) + PANB()
				* log2(div(PANB(), PA() * (1.0 - PB()))) + PNAB()
				* log2(div(PNAB(), (1.0 - PA()) * PB())) + PNANB()
				* log2(div(PNANB(), (1.0 - PA()) * (1.0 - PB())));
	}

	@Override
	public String getCoefficientName() {
		return "Two-Way Support Variation";
	}

}
