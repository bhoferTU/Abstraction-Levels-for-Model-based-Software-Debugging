package at.tugraz.ist.debugging.modelbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.util.AlphanumComparator;

public class Diagnosis extends HashSet<Coords> implements Comparable<Diagnosis> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//Set<Coords> this = new HashSet<Coords>();

	public Diagnosis addFaultCell(Cell faultyCell) {
		this.add(faultyCell.getCoords());
		return this;
	}

	@Override
	public int compareTo(Diagnosis cmp) {
		// cardinality comparison
		if (this.size() < cmp.size())
			return -1;
		if (this.size() > cmp.size())
			return 1;

		// internal structure check
		return new AlphanumComparator<String>().compare(toString(),
				cmp.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Diagnosis))
			return false;

		return compareTo((Diagnosis) obj) == 0;
	}

	public Set<Coords> getCells() {
		return this;
	}

	private List<String> getSortedCSVCellList() {
		List<String> csvCellStrings = new ArrayList<String>();
		for (Coords cell : this) {
			if (cell == null)
				continue;
			csvCellStrings.add(cell.getCSVString());
		}
		Collections.sort(csvCellStrings);
		return csvCellStrings;
	}

	public boolean removeInputCells(Set<Cell> inputCells, Set<Cell> cones) {
		for (Cell cell : inputCells)
            if(cones != null && !cones.contains(cell))
                this.remove(cell.getCoords());
		return this.size() != 0;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
        boolean first = true;
		for (String cellString : getSortedCSVCellList()) {
            if(!first)
                s.append(", ");
			s.append(cellString);
            first = false;
		}

		s.insert(0, "(");
		s.append(")");
		return s.toString();
	}
	
}
