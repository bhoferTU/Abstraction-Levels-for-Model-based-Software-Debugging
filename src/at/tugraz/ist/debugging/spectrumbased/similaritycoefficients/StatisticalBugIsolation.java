package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

/**
 * Computes the similarity coefficient for the given hit spectra by using the
 * StatisticalBugIsolation coefficient.
 * 
 * <pre>
 * coefficient = a11/(a11+a10)
 * a11 = FailedInvolved
 * a10 = PassedInvolved
 * </pre>
 * 
 * @author bhofer
 * 
 */
public class StatisticalBugIsolation extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		double value = 0;
		if (a11 + a10 != 0)
			value = (double) a11 / (a11 + a10);
		return value;
	}

	@Override
	public String getCoefficientName() {
		return "SBI";
	}

}
