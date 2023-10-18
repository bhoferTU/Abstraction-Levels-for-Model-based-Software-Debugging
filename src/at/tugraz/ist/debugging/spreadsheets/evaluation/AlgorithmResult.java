package at.tugraz.ist.debugging.spreadsheets.evaluation;

import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.evaluation.ranking.IRanking;

public abstract class AlgorithmResult extends Result {

	public abstract IRanking<Coords> getRanking();

}
