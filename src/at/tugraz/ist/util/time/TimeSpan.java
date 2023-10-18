package at.tugraz.ist.util.time;

import java.io.Serializable;

public class TimeSpan implements Serializable {
	public enum Precision implements Serializable {
		MICROSECONDS(1000000, "micros"), MILLISECONDS(1000, "ms"), NANOSECONDS(
				1000000000, "ns"), SECONDS(1, "s");

		String abbreviation;
		long invFactor;

		private Precision(long invFactor, String abbreviation) {
			this.invFactor = invFactor;
			this.abbreviation = abbreviation;
		}

		public String getAbbreviation() {
			return abbreviation;
		}

		public long getInverseFactor() {
			return invFactor;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2211892971077237114L;;

	private Precision precision;
	private long time;

	protected TimeSpan() {

	}

	public TimeSpan(Double time, Precision precision) {
		long invFactor = Precision.NANOSECONDS.getInverseFactor()
				/ precision.getInverseFactor();
		time *= invFactor;
		this.time = time.longValue();
		this.precision = Precision.NANOSECONDS;
	}

	public TimeSpan(long time, Precision precision) {
		this.time = time;
		this.precision = precision;
	}

	public TimeSpan(Precision precision) {
		this(0, precision);
	}

	public TimeSpan(TimeSpan ts) {
		this.precision = ts.precision;
		this.time = ts.getTimeSpan(ts.precision);
	}

	public void add(TimeSpan timeSpan) {
		this.time += timeSpan.getTimeSpan(precision);
	}

	public void divide(int size) {
		time /= size;
	}

	public Precision getPrecision() {
		return precision;
	}

	public long getTimeSpan(Precision targetPrecision) {
		if (precision == targetPrecision)
			return time;

		long targetFactor = targetPrecision.getInverseFactor();
		long sourceFactor = precision.getInverseFactor();

		return (time * targetFactor) / sourceFactor;
	}

	public TimeSpan pow(int exponent) {
		this.time = ((Double) Math.pow(time, exponent)).longValue();
		return this;
	}

	public void sqrt() {
		this.time = ((Double) Math.sqrt(time)).longValue();

	}

	public TimeSpan subtract(TimeSpan timeSpan) {
		this.time -= timeSpan.getTimeSpan(precision);
		return this;
	}

	@Override
	public String toString() {
		return time + precision.getAbbreviation();
	}

	public String toString(Precision precision) {
		return getTimeSpan(precision) + precision.getAbbreviation();
	}

}
