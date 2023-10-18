package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class CollectiveStrength extends SimilarityCoefficient {

	@Override
	public Double calculateCoefficient() {
		double term1 = div(PAB() + PNANB(), PA() * PB() + (1.0 - PA())
				* (1.0 - PB()));
		double term2 = div(1.0 - PA() * PB() - (1.0 - PA()) * (1.0 - PB()), 1.0
				- PAB() - PNANB());
		return term1 * term2;
	}

	@Override
	public String getCoefficientName() {
		return "Collective Strength";
	}

}
