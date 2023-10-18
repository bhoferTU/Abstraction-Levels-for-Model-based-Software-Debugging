package at.tugraz.ist.debugging.modelbased.minion;

import java.util.HashSet;
import java.util.Set;

import at.tugraz.ist.debugging.modelbased.minion.MinionConstraintStrategyGenerationInformation.Domain;

public class MinionExpressionConstraints {

	private Set<String> constraints = null;
	private Set<String> constraintsTC = null;
	boolean isConstant = false;
	private Domain resultType;
	private String varname = null;
	//boolean inIfBranch=false;

	public MinionExpressionConstraints(Set<String> constraints,
			Set<String> constraintsTC, String variableName, Domain resultType) {
		this.constraints = constraints;
		varname = variableName;
		this.resultType = resultType;
		this.constraintsTC = constraintsTC;
	}

	public MinionExpressionConstraints(Set<String> constraints,
			Set<String> constraintsTC, String variableName, Domain resultType,
			boolean isConstant) {
		this.constraints = constraints;
		varname = variableName;
		this.resultType = resultType;
		this.constraintsTC = constraintsTC;
		this.isConstant = isConstant;
	}
	
	public MinionExpressionConstraints(Set<String> constraints,
			String variableName, Domain resultType) {
		this.constraints = constraints;
		varname = variableName;
		this.resultType = resultType;
		this.constraintsTC = new HashSet<String>();
	}

	public void addConstraints(MinionExpressionConstraints constraints) {
		this.constraints.addAll(constraints.getConstraints());
		this.constraintsTC.addAll(constraints.getConstraintsTC());
	}

	public Set<String> getConstraints() {
		return constraints;
	}

	public Set<String> getConstraintsTC() {
		return constraintsTC;
	}

	public Domain getResultType() {
		return resultType;
	}

	public String getVarname() {
		return varname;
	}

	public Boolean isConstant() {
		return isConstant;
	}

}
