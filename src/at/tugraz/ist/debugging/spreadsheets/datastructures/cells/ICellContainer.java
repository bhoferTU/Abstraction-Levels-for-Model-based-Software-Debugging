package at.tugraz.ist.debugging.spreadsheets.datastructures.cells;

import java.util.Set;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;

public interface ICellContainer {

	public Set<Coords> getFormulaCoords();

	public Set<Coords> getInputCoords();

	public Set<Coords> getOutputCoords();

	public ICell getICell(Coords coord);

	//void populate(String filepath, boolean silent) throws Exception;

	public void populate(String filepath);
}
