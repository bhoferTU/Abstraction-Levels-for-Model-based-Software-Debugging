package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class GiniIndex extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = PA()
				* (PB_given_A() * PB_given_A() + (1.0 - PB_given_A())
						* (1.0 - PB_given_A()))
				+ (1.0 - PA())
				* (PB_given_NA() * PB_given_NA() + (1.0 - PB_given_NA())
						* (1.0 - PB_given_NA())) - PB() * PB() - (1.0 - PB())
				* (1.0 - PB());

		double term2 = PB()
				* (PA_given_B() * PA_given_B() + (1.0 - PA_given_B())
						* (1.0 - PA_given_B()))
				+ (1.0 - PB())
				* (PA_given_NB() * PA_given_NB() + (1.0 - PA_given_NB())
						* (1.0 - PA_given_NB())) - PA() * PA() - (1.0 - PA())
				* (1.0 - PA());

		return Math.max(term1, term2);
	}

	@Override
	public String getCoefficientName() {
		return "Gini Index";
	}

}
