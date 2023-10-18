package at.tugraz.ist.debugging.modelbased;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import at.tugraz.ist.util.time.TimeSpan.Precision;

public class SharedProperties {
	private static final String DEFAULT_PROPERTIES_FILE_PATH = "mussco.config";

	private static SharedProperties instance = null;

	public static SharedProperties getInstance() {
		if (instance == null)
			instance = new SharedProperties(DEFAULT_PROPERTIES_FILE_PATH);

		return instance;
	}

	public static SharedProperties getInstance(String sharedPropertiesFileName) {
		if (instance != null)
			throw new RuntimeException(
					"SharedProperties configuration is already initialized");
		instance = new SharedProperties(sharedPropertiesFileName);
		return instance;
	}

	private String path;
	private Properties properties;
	private Random randGen = new Random();
	private Long timeout = null;

	public SharedProperties(String path) {
		this.path = path;
		properties = new Properties();
		try {
			BufferedInputStream stream = new BufferedInputStream(
					new FileInputStream(path));
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			// catch silently
			// System.out.println(String.format("Cannot load shared properties file %s",
			// path));
			// throw new RuntimeException(e);
		}
	}

	public boolean checkStrategyResultEquality() {
		return Boolean.parseBoolean(properties.getProperty(
				"benchmark.checkStrategyResultEquality", "false"));
	}

	public boolean debugWriteFormula() {
		return Boolean.parseBoolean(properties.getProperty(
				"debug.useFormulaFiles", "false"));
	}

	public boolean deleteTemporaryFiles() {
		return Boolean.parseBoolean(properties.getProperty(
				"benchmark.temporary.delete", "false"));
	}

	public boolean determineAllDiagnosesFirst() {
		return Boolean.parseBoolean(properties.getProperty(
				"strategies.mutation.determineAllDiagnosesFirst", "false"));
	}

	public Long getBenchmarkTimeout() {
		if (timeout != null)
			return timeout;
		return Long.valueOf(properties.getProperty("benchmark.timeout",
				"1200000"));
	}

	public String getDebugFormulaPath() {
		return properties.getProperty("debug.formulaFilePath");
	}

	public boolean getMutationStrategyDynamicValueSupportActive() {
		return Boolean.parseBoolean(properties.getProperty(
				"strategies.mutation.dynamicValueSupportActive", "true"));
	}

	public int getMutationStrategySupportedUpperBound() {
		return Integer.valueOf(properties.getProperty(
				"strategies.mutation.diagnosesUpperBound", "1"));
	}

	public String getPath() {
		return path;
	}

	public Precision getPrecision() {
		return Precision.valueOf(properties.getProperty(
				"measurement.precision", "MILLISECONDS").toUpperCase());
	}

	public int getRunsAmount() {
		return Integer.valueOf(properties.getProperty("benchmark.runs", "1"));
	}

	public String[] getSupportedMutationOperators() {
		String mutationOperatorsListString = properties.getProperty(
				"strategies.mutation.mutationOperators", null);
		if (mutationOperatorsListString == null)
			return null;

		String[] mutationOperatorsArray = mutationOperatorsListString
				.split("\\s*,\\s*");
		return mutationOperatorsArray;
	}

	public String getTemporaryBenchmarkErrorPath() {
		String path = properties.getProperty(
				"benchmark.temporary.errorFilePath", "err_<ts>_<rand>.tmp");
		return renderTemplate(path);
	}

	public String getTemporaryBenchmarkOutputPath() {
		String path = properties.getProperty(
				"benchmark.temporary.outputFilePath", "out_<ts>_<rand>.tmp");
		return renderTemplate(path);

	}

	private String renderTemplate(String path) {
		path = path.replaceAll("<ts>",
				String.valueOf(Calendar.getInstance().getTime().getTime()));
		path = path
				.replaceAll("<rand>", String.valueOf(randGen.nextInt(16536)));
		return path;
	}

	public boolean restrictIntegerValues() {
		return Boolean.parseBoolean(properties.getProperty(
				"strategies.mutation.restrictIntegerValues", "false"));
	}

	public void setBenchmarkTimeout(long timeout) {
		this.timeout = timeout;
	}

	public boolean showExcelFile() {
		return Boolean.parseBoolean(properties.getProperty(
				"benchmark.showExcelFile", "false"));
	}

	public boolean showKillingTestcasesForEachMutation() {
		return Boolean.parseBoolean(properties.getProperty(
				"logging.showKillingTestcasesForEachMutation", "true"));
	}

	public boolean useCones() {
		return Boolean.parseBoolean(properties.getProperty(
				"strategies.useCones", "false"));
	}

	public boolean useEarlyTermination() {
		return Boolean.parseBoolean(properties.getProperty(
				"strategies.useEarlyTermination", "false"));
	}

	public boolean useStrings() {
		return Boolean.parseBoolean(properties.getProperty(
				"strategies.useStrings", "false"));
	}

}
