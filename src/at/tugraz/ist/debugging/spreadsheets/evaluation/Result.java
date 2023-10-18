package at.tugraz.ist.debugging.spreadsheets.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.tugraz.ist.util.Messages;

/**
 * A simple, but flexible class that can be used for storing results
 * 
 * @author bhofer
 * 
 */
public class Result {

	static List<String> columnHeaders = null;

	final static String SEPARATOR = Messages.getString("Seperator0");

	public static void clear() {
		columnHeaders = new ArrayList<String>();
	}

	public static String getColumnHeader() {
		StringBuilder strB = new StringBuilder();
		if(columnHeaders == null)
			throw new NullPointerException("Please add data before getting the ColumnHeader");
		for (String columnHead : columnHeaders) {
			strB.append(columnHead);
			strB.append(SEPARATOR);
		}
		strB.append(System.getProperty("line.separator"));
		return strB.toString();
	}

	Map<String, String> resultsMap = new HashMap<String, String>();

	public Result() {
		if (columnHeaders == null)
			columnHeaders = new ArrayList<String>();
	}

	public void addData(String key, String value) {
		if (!columnHeaders.contains(key))
			columnHeaders.add(key);
		if(value.length()>30000)
			value = value.substring(0, 30000)+"...";
		resultsMap.put(key, value);
	}
	
	public void addData(String key, Integer value) {
		if (!columnHeaders.contains(key))
			columnHeaders.add(key);
		resultsMap.put(key, value.toString());
	}

	public List<String> getColumnHeaders() {
		return columnHeaders;
	}

	public String getResultEntry(String name) {
		if (resultsMap.containsKey(name))
			return resultsMap.get(name);
		else
			return "-";
	}

	@Override
	public String toString() {
		StringBuilder strB = new StringBuilder();

		for (String columnHead : columnHeaders) {
			strB.append(getResultEntry(columnHead));
			strB.append(SEPARATOR);
		}
		strB.append(System.lineSeparator());
		return strB.toString();
	}
}
