package at.tugraz.ist.debugging.modelbased.main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.Diagnosis;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.debugging.spreadsheets.evaluation.Result;

public class ModelComparisionResult extends Result {

	public ModelComparisionResult(String propertiesFile,
			List<Diagnosis> valueBasedDiagnoses,
			List<Diagnosis> simpleDependencyDiagnoses,
			List<Diagnosis> sophisticatedDependencyDiagnoses,
			Long valueBasedTime, Long simpleDependencyBasedTime,
			Long sophisticatedDependencyBasedTime) {
		addData("Properties file", propertiesFile);
		addData("value based diagnoses (VBD)",
				diagnosisToString(valueBasedDiagnoses));
		addData("simple dependency based diagnoses",
				diagnosisToString(simpleDependencyDiagnoses));
		addData("sophisticted dependency based diagnoses",
				diagnosisToString(sophisticatedDependencyDiagnoses));

		addData("value based time [ms]", valueBasedTime.toString());
		addData("simple dependency based time [ms]",
				simpleDependencyBasedTime.toString());
		addData("sophisticted dependency based time [ms]",
				sophisticatedDependencyBasedTime.toString());
		addData("Number of cells in VBD",
				getNumberOfCellsInDiagnoses(valueBasedDiagnoses).toString());
		addData("Number of cells in SimpleDBD",
				getNumberOfCellsInDiagnoses(simpleDependencyDiagnoses)
						.toString());
		addData("Number of cells in SophDBD",
				getNumberOfCellsInDiagnoses(sophisticatedDependencyDiagnoses)
						.toString());
		addData("All VBD containd in SophDBD",
				areAllDiagnosesContained(sophisticatedDependencyDiagnoses,
						valueBasedDiagnoses) ? "true" : "false");
	}

	private boolean areAllDiagnosesContained(List<Diagnosis> superset,
			List<Diagnosis> subset) {
		for (Diagnosis diagnosis : subset) {
			if (superset.contains(diagnosis))
				continue;

			boolean contained = false;
			for (Diagnosis diag : superset) {
				if (isSubDiagnosis(diagnosis, diag)) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				System.out.println(super.getResultEntry("Properties file")
						+ ": Diagnosis " + diagnosis.toString()
						+ " not contained!");
				return false;
			}
		}
		return true;
	}

	private String diagnosisToString(List<Diagnosis> diagnoses) {
		StringBuilder strB = new StringBuilder();
		for (Diagnosis diagnosis : diagnoses) {
			strB.append("(");
			for (Coords cell : diagnosis.getCells()) {
				strB.append(cell.getConstraintString());
				strB.append(",");
			}
			strB.append("),");
		}
		String str = strB.toString().replace(",)", (")"));
		if (str.length() > 0)
			return str.substring(0, str.length() - 1);
		return "";
	}

	private Integer getNumberOfCellsInDiagnoses(List<Diagnosis> diagnoses) {
		Set<Coords> cells = new HashSet<Coords>();
		for (Diagnosis diagnosis : diagnoses) {
			cells.addAll(diagnosis.getCells());
		}
		return cells.size();
	}

	private boolean isSubDiagnosis(Diagnosis superDiagnosis,
			Diagnosis subDiagnosis) {
		for (Coords subCell : subDiagnosis.getCells()) {
			if (!superDiagnosis.getCells().contains(subCell))
				return false;
			/*
			boolean contained = false;
			for (Coords superCell : superDiagnosis.getCells()) {
				if (subCell.toString().equalsIgnoreCase(superCell.toString())) {
					contained = true;
					break;
				}
			}
			if (!contained) {
				return false;
			}*/
		}
		return true;
	}
}
