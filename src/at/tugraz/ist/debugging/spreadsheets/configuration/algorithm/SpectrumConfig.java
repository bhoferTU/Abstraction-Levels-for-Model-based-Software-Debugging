package at.tugraz.ist.debugging.spreadsheets.configuration.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.datastructures.ObservationMatrix;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.ICell;
import at.tugraz.ist.util.datastructures.Pair;

public class SpectrumConfig extends SpreadsheetInput {

	private boolean debug = false;
	
	private boolean dynamic = false;
	
	// per default (in EUSES), faulty cells may be input/constant cells
	private boolean includeConstants = true;
	

	protected List<Set<Coords>> negativeCones;
	protected ObservationMatrix obs;
	protected List<Set<Coords>> positiveCones;

	

	protected Set<Coords> union;

	public SpectrumConfig(SpreadsheetProperties properties, CellContainer cells) {
		
		super(properties, cells);
		
		if (needsInit)
			init();

	}
	

	public static SpectrumConfig create(String properties) throws SpreadsheetPropertiesException
	{
		SpreadsheetProperties props = new SpreadsheetProperties(properties);
		CellContainer container = CellContainer.create(props.getExcelSheetPath());
		return new SpectrumConfig(props, container);
	}
	
	public List<Set<Coords>> getCones(List<Coords> cellList) {
		
		List<Set<Coords>> cones = new ArrayList<Set<Coords>>();
		for (Coords cell : cellList) {
			ICell aCell = cells.getICell(cell);
			if (aCell == null) {
				System.err.println("cell: " + cell + " is null");
				for (Cell allCellsCell: cells.getCells())
					System.out.println(allCellsCell.getCoords());
				
				continue;
			}
			
			Set<Coords> cone = (aCell).getCone(dynamic, includeConstants);
			cones.add(cone);
		}
		return cones;
	}

	public Pair<Integer, Boolean> getIntersection(List<Set<Coords>> negatives,
			List<Coords> faulty) {
		Set<Coords> allNeg = null;
		Set<Coords> remove = new HashSet<Coords>();

		for (Set<Coords> cone : negatives) {
			if (allNeg == null) {
				allNeg = cone;
			} else {
				for (Coords interCell : allNeg) {
					if (!cone.contains(interCell)) {
						remove.add(interCell);
					}
				}
				for (Coords removeCell : remove) {
					allNeg.remove(removeCell);
				}
			}
		}
		Boolean contains = false;
		for (Coords faultyCell : faulty) {
			if (allNeg.contains(faultyCell)) {
				contains = true;
			}
		}
		if (debug) {
			System.out.println("Intersection: ");
			for (Coords cell : allNeg) {
				System.out.print(cell.getShortString() + " ");
			}
			System.out.println();
			System.out.println("contained: " + contains);
		}

		return new Pair<Integer, Boolean>(allNeg.size(), contains);
	}

	public List<Set<Coords>> getNegativeCones() {
		
		if (needsInit)
			init();
		
		return negativeCones;
	}

	public ObservationMatrix getObservationMatrix() {
		
		if (needsInit)
			init();

		return obs;
	}

	public List<Set<Coords>> getPositiveCones() {
		
		if (needsInit)
			init();
		
		return positiveCones;
	}

	public Pair<Integer, Integer> getRankingOfFaultyCells(
			Map<Coords, Integer> ranking, List<Coords> faultyCells) {
		Integer minRanking = ranking.size();
		Integer same = -1;
		for (Coords cell : faultyCells) {
			if (ranking.get(cell) < minRanking) {
				minRanking = ranking.get(cell);
			}
		}
		for (Integer rank : ranking.values()) {
			if (rank == minRanking)
				same++;
		}
		return new Pair<Integer, Integer>(minRanking, same);
	}

	public Set<Coords> getUnion() {
		return union;
	}

	public Set<Coords> getUnionSize(List<Set<Coords>> negatives) {
		Set<Coords> allNeg = new HashSet<Coords>();
		for (Set<Coords> cone : negatives) {
			allNeg.addAll(cone);
		}
		if (debug) {
			System.out.println("UNION: ");
			for (Coords cell : allNeg) {
				System.out.print(cell.getShortString() + " ");
			}
			System.out.println();
		}
		return allNeg;
	}

	@Override
	public void init() {
		List<Coords> positives = properties.getCorrectOutputCells();
		positiveCones = getCones(positives);

		List<Coords> negatives = properties.getIncorrectOutputCells();
		negativeCones = getCones(negatives);

		union = getUnionSize(negativeCones);
		obs = new ObservationMatrix(positiveCones, negativeCones, union);
		
		needsInit = false;
	}

	public void printCones(List<Set<Coords>> cones) {
		for (Set<Coords> cone : cones) {
			for (Coords cell : cone) {
				System.out.print(cell.getShortString() + "   ");
			}
			System.out.println();
		}
	}

	public void setDebug(boolean dbg) {
		this.debug = dbg;
	}

	public boolean isDynamic()
	{
		return dynamic;
	}
	
	public boolean isConstantEnabled()
	{
		return includeConstants;
	}
	
	public void setDynamic(boolean selected) {
		if (dynamic == selected)
			return;
		
		dynamic = selected;
		needsInit = true;
	}

	public void setIncludeConstants(boolean selected) {
		if (includeConstants == selected)
			return;
		
		includeConstants = selected;
		needsInit = true;
	}

	public boolean hasChanged() {
		return needsInit;
	}

}
