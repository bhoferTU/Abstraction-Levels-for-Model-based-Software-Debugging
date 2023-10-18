package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class GoodmanKruskal extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double term1 = Math.max(PAB(), PANB()) + Math.max(PNAB(), PNANB());
		double term2 = Math.max(PAB(), PNAB()) + Math.max(PANB(), PNANB());
		double term3 = Math.max(PA(), 1.0 - PA());
		double term4 = Math.max(PB(), 1.0 - PB());

		return div(term1 + term2 - term3 - term4, 2.0 - term3 - term4);
	}

	@Override
	public String getCoefficientName() {
		return "Goodman and Kruskal";
	}

}
