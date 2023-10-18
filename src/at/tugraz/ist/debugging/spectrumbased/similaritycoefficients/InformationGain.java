package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

public class InformationGain extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double result = (-PB() * log(PB()) - (1.0 - PB()) * log(1.0 - PB()))
				- (PA() * (-PB_given_A() * log(PB_given_A())))
				- ((1.0 - PB_given_A()) * log(1.0 - PB_given_A()))
				- ((1.0 - PA()) * (-PB_given_NA() * log(PB_given_NA())))
				- ((1.0 - PB_given_NA()) * log(1.0 - PB_given_NA()));

		return result;
	}

	@Override
	public String getCoefficientName() {
		return "Information Gain";
	}

}
