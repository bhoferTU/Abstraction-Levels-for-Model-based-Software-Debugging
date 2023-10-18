package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class PhiCoefficient extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		return div(PAB() - PA() * PB(),
				Math.sqrt(PA() * PB() * (1.0 - PA()) * (1.0 - PB())));
	}

	@Override
	public String getCoefficientName() {
		return "Phi-Coefficient";
	}

}
