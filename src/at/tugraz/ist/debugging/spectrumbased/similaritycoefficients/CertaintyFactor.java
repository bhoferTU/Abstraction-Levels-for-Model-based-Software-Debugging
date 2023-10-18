package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class CertaintyFactor extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = 0.0;
		double term2 = 0.0;
		double div = 1.0 - PB();
		if (div != 0)
			term1 = (PB_given_A() - PB()) / div;
		div = 1.0 - PA();
		if (div != 0)
			term2 = (PA_given_B() - PA()) / div;

		return Math.max(term1, term2);
	}

	@Override
	public String getCoefficientName() {
		return "Certainty Factor";
	}

}
