package at.tugraz.ist.util.time;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import at.tugraz.ist.debugging.modelbased.SharedProperties;
import at.tugraz.ist.debugging.modelbased.evaluation.IXmlLoggable;

public class TimeSpanAverage implements IXmlLoggable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7290840666523511674L;
	private boolean showAll;
	List<TimeSpan> timeSpans;

	public TimeSpanAverage() {
		this.timeSpans = new ArrayList<TimeSpan>();
	}

	public TimeSpanAverage(boolean showAll) {
		this();
		this.showAll = showAll;
	}

	public void addTimeSpan(TimeSpan timeSpan) {
		timeSpans.add(timeSpan);
	}

	public TimeSpan getAccumulatedTimeSpan() {
		TimeSpan overallTimeSpan = new TimeSpan(SharedProperties.getInstance()
				.getPrecision());
		for (TimeSpan ts : timeSpans) {
			if (ts == null)
				return new TimeoutTimeSpan();
			overallTimeSpan.add(ts);
		}
		return overallTimeSpan;
	}

	public TimeSpan getAverageTimeSpan() {
		TimeSpan overallTimeSpan = getAccumulatedTimeSpan();
		if (timeSpans.size() == 0)
			return overallTimeSpan;
		overallTimeSpan.divide(timeSpans.size());
		return overallTimeSpan;
	}

	public TimeSpan getStdDev() {
		if (timeSpans.size() <= 1)
			return new TimeSpan(SharedProperties.getInstance().getPrecision());
		TimeSpan avg = getAverageTimeSpan();
		if (avg instanceof TimeoutTimeSpan)
			return new TimeoutTimeSpan();
		TimeSpan stdDev = new TimeSpan(SharedProperties.getInstance()
				.getPrecision());
		for (TimeSpan ts : timeSpans) {
			stdDev.add(new TimeSpan(ts).subtract(avg).pow(2));

		}
		stdDev.divide(timeSpans.size() - 1);
		stdDev.sqrt();
		return stdDev;
	}

	@Override
	public Element getXMLNode(Document document) {
		Element element = document.createElement("timespan");
		Element sumElement = document.createElement("sum");
		sumElement.setTextContent(getAccumulatedTimeSpan().toString(
				SharedProperties.getInstance().getPrecision()));
		element.appendChild(sumElement);
		Element avgElement = document.createElement("avg");
		avgElement.setTextContent(getAverageTimeSpan().toString(
				SharedProperties.getInstance().getPrecision()));
		element.appendChild(avgElement);
		Element stdDevElement = document.createElement("stdDev");
		stdDevElement.setTextContent(getStdDev().toString(
				SharedProperties.getInstance().getPrecision()));
		element.appendChild(stdDevElement);

		if (showAll) {
			Element allElements = document.createElement("items");
			for (TimeSpan ts : timeSpans) {
				Element tsElement = document.createElement("item");
				tsElement.setTextContent(ts.toString());
				allElements.appendChild(tsElement);
			}
			element.appendChild(allElements);
		}

		return element;
	}
}
