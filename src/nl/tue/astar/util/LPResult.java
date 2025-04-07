package nl.tue.astar.util;

import lpsolve.LpSolve;

public class LPResult {

	protected final int solveResult;

	@Deprecated
	public LPResult(int variableCount, double result) {
		this(variableCount, result, LpSolve.OPTIMAL);
	}

	public LPResult(int variableCount, double result, int solveResult) {
		this.variables = new double[variableCount];
		this.result = result;
		this.solveResult = solveResult;
	}

	public LPResult(double[] variables, double result, int solveResult) {
		this.variables = variables;
		this.result = result;
		this.solveResult = solveResult;
	}

	public int getSolveResult() {
		return solveResult;
	}

	public double[] getVariables() {
		return variables;
	}

	public double getResult() {
		return result;
	}

	private final double[] variables;

	private final double result;

	public double getVariable(int i) {
		return variables[i];
	}

}
