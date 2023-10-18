package at.tugraz.ist.debugging.spectrumbased.similaritycoefficients;

/**
 * Computes the similarity coefficient for the given hit spectra by using the
 * Tarantula coefficient.
 * 
 * <pre>
 * coefficient = [a11/(a11+a01)]/[[a11/(a11+a01)]+[a10/(a10+a00))]]
 * a11 = FailedInvolved
 * a10 = PassedInvolved 
 * a01 = FailedNotInvolved
 * a00 = PassedNotInvolved
 * </pre>
 * 
 * Additionally it is possible to compute the confidence (max(%failed, %passed))
 * 
 * @author bhofer
 * 
 */
public class Tarantula extends SimilarityCoefficient {

	@Override
	protected Double calculateCoefficient() {
		if (a11 == 0)
			return 0.0;
		double i = (double) a11 / (a11 + a01);
		double j = 0;
		if (a10 != 0)
			j = (double) a10 / (a10 + a00);
		if (i + j != 0)
			return (i / (i + j));
		return 0.0;
	}

	@Override
	public String getCoefficientName() {
		return "Tarantula";
	}

	/**
	 * Computes the confidence (max(%failed, %passed)) for the given spectra
	 * information
	 * 
	 * @param a11
	 *            Number of failed and involved test cases
	 * @param a10
	 *            Number of passed and involved test cases
	 * @param a01
	 *            Number of failed and not involved test cases
	 * @param a00
	 *            Number of passed and not involved test cases
	 * @return Confidence (max(%failed, %passed))
	 */
	public Double getConfidence(int a11, int a10, int a01, int a00) {

		double percFailed = 0.0;
		if ((a11 + a01) != 0)
			percFailed = (double) a11 / (a11 + a01);
		double percPassed = 0.0;
		if ((a10 + a00) != 0)
			percPassed = (double) a10 / (a10 + a00);

		if (percFailed > percPassed)
			return percFailed;
		return percPassed;
	}

}
