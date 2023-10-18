package at.tugraz.ist.util.time;


import at.tugraz.ist.util.time.TimeSpan.Precision;

public class TimeSpanMeasurement {
	private Precision precision;

	private long startValue;

	public TimeSpanMeasurement(Precision precision) {
		this.precision = precision;
	}

	private long getCurrentTimestamp() {
		switch (precision) {
		case MILLISECONDS:
			return System.currentTimeMillis();
		case NANOSECONDS:
			return System.nanoTime();
		default:
			throw new UnsupportedOperationException(
					"Please measure in [ns] or [ms]");
		}
	}

	public void start() {
		if (startValue != 0)
			System.err.println(
					"Previous measurement was not stopped!");

		startValue = getCurrentTimestamp();
	}

	public TimeSpan stop() {
		long endValue = getCurrentTimestamp();
		if (startValue == 0)
			System.err.println(
					"Measurement was stopped without starting it before");
		TimeSpan timeSpan = new TimeSpan(endValue - startValue, precision);
		startValue = 0;
		return timeSpan;
	}
}
