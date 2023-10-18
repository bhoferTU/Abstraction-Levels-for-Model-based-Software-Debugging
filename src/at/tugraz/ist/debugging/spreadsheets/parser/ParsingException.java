package at.tugraz.ist.debugging.spreadsheets.parser;

import java.util.ArrayList;

import org.apache.poi.ss.formula.ptg.Ptg;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;

/**
 * Exception which can occur while parsing
 * 
 */
public class ParsingException extends RuntimeException {

	private static final long serialVersionUID = 6125778373544043809L;

	String cellName = "?";
	ArrayList<Ptg> ptgs = null;

	public ParsingException(String message) {
		super(message);
	}

	@Override
	public String getMessage() {
		if (ptgs == null)
			return cellName + ": " + super.getMessage();
		String msg = cellName + ": " + super.getMessage();
		for (Ptg ptg : ptgs)
			msg += "\n     " + ptg;
		return msg;
	}

	public ParsingException setCell(Coords cellName) {
		this.cellName = cellName.getUserString();
		return this;
	}

	public ParsingException setPtgs(Ptg[] ptgs) {
		this.ptgs = new ArrayList<Ptg>(ptgs.length);
		for (Ptg ptg : ptgs)
			this.ptgs.add(ptg);
		return this;
	}
}
