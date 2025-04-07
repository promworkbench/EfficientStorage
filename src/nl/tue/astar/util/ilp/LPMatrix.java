package nl.tue.astar.util.ilp;

import gurobi.GRBEnv;

import java.io.IOException;
import java.io.OutputStreamWriter;

import lpsolve.LpSolve;

/**
 * This interface represents an LP matrix, consisting of an objective function,
 * an internal matrix, a right-hand-side vector, row- and column names, variable
 * types, constraint types and lower and upper bound. The type parameter<S>
 * refers to the type of the external solver used for solving the MIP
 * represented by this object.
 * 
 * The interface is borrowed mainly from LpSolve, with the distinct difference
 * that all rows and columns start from 0, i.e. there are [0 .. getNRows()-1]
 * rows and [0 .. getNColumns()-1] columns.
 * 
 * The constants GE (greater or equal), LE (less or equal), and EQ (equal)
 * represent the constraint types
 * 
 * The constants REAL, INTEGER and BINARY represent the variable types.
 * 
 * INFINITE = 1E30 defines infinity.
 * 
 * A call to solve(double[] vars) will push the internal structure to the
 * solver, call the external solve function (currently provided for LpSolve and
 * Gurobi) and return OPTIMAL, in case an optimal solution is found, INFEASIBLE,
 * if the model is infeasible or unbounded, or OTHER, if the external solver
 * returned another result.
 * 
 * If the solve() call returns OPTIMAL, the array vars is filled with the
 * solution for all variables, i.e. vars needs to be dimensioned
 * [0..getNColumns()-1] by the caller.
 * 
 * If the solve() call returns INFEASIBLE or OTHER, the vars array is not
 * touched. The exact result of the external solver can be obtained by a call to
 * getLastSolverResult().
 * 
 * An LPMatrixException is thrown in all methods that are pushed to the external
 * solver and the exception typically wraps the exception thrown by the external
 * solver.
 * 
 * The SPARSE and DENSE implementations provided by this interface differ in the
 * way the internal matrix is stored. The SPARSE variants use considerably less
 * memory at the expense of slightly higher time to set elements in the matrix.
 * 
 * Additional to an esay solve() method, the interface also provides a
 * toSolver() method, that returns an object of type S, which is a translation
 * of the matrix into the external solver. This method is for more advanced use
 * only. Note that the getOffset() method is to be used in conjunction to make
 * sure that rows and columns are adressed properly in the external
 * representation.
 * 
 * 
 * @author bfvdonge
 * 
 * @param <S>
 */
public interface LPMatrix<S> {

	public static final int INFEASIBLE = LpSolve.INFEASIBLE;
	public static final int OPTIMAL = LpSolve.OPTIMAL;
	public static final int OTHER = -1;

	public static final byte REAL = 0;
	public static final byte INTEGER = 1;
	public static final byte BINARY = 2;

	public static final byte GE = LpSolve.GE;
	public static final byte LE = LpSolve.LE;
	public static final byte EQ = LpSolve.EQ;

	public static double INFINITE = 1.0e30;

	/**
	 * Static class wrapping DENSE variants of LPMatrix<S>
	 * 
	 * The dense variants of LPMatrix<S> represent the internal MIP matrix
	 * explicitly, i.e. for each element in the matrix, there is a double in
	 * memory. Therefore the memory use of these implementations is large.
	 * 
	 * Getting and setting values in the matrix is fast, i.e. both O(1)
	 */
	public final static class DENSE {

		/**
		 * The dense variant for the LpSolve external solver.
		 */
		public static class LPSOLVE extends DenseLPMatrix.LPSOLVE {

			public LPSOLVE(int rows, int columns) {
				super(rows, columns);
			}

			public LPSOLVE(LPMatrix<?> matrix) {
				super(matrix);
			}

		}

		/**
		 * The dense variant for the Gurobi external solver. Needs a
		 * GurobiEnvironment as input.
		 */
		public static class GUROBI extends DenseLPMatrix.GUROBI {

			public GUROBI(GRBEnv env, int rows, int columns) {
				super(env, rows, columns);
			}

			public GUROBI(GRBEnv env, LPMatrix<?> matrix) {
				super(env, matrix);
			}

		}
	}

	/**
	 * Static class wrapping SPARSE variants of LPMatrix<S>
	 * 
	 * The internal MIP matrix is represented by a hashmap from a pair of
	 * coordinates to a value. Setting a value in this matrix is O(log N), while
	 * reading a value is amortized O(1). When adjusting a value, the method
	 * adjustMat(r, c, v) is recommended over setMat(r,c, getMat(r,c) + v).
	 * 
	 */
	public final static class SPARSE {
		public static class LPSOLVE extends SparseLPMatrix.LPSOLVE {

			/**
			 * The sparse variant for the LpSolve external solver.
			 */
			public LPSOLVE(int rows, int columns) throws LPMatrixException {
				super(rows, columns);
			}

			public LPSOLVE(LPMatrix<?> matrix) throws LPMatrixException {
				super(matrix);
			}

		}

		/**
		 * The sparse variant for the Gurobi external solver. Needs a
		 * GurobiEnvironment as input.
		 */
		public static class GUROBI extends SparseLPMatrix.GUROBI {

			public GUROBI(GRBEnv env, int rows, int columns) throws LPMatrixException {
				super(env, rows, columns);
			}

			public GUROBI(GRBEnv env, LPMatrix<?> matrix) throws LPMatrixException {
				super(env, matrix);
			}

		}

	}

	/**
	 * Constructs the solver in its external representation
	 * 
	 * @return
	 * @throws LPMatrixException
	 */
	public S toSolver() throws LPMatrixException;

	/**
	 * Constructs the solver and solves the MIP represented by this problem. The
	 * returned value is either OPTIMAL, INFEASIBLE or OTHER. The external
	 * solver's result can be obtained through getLastSolverResult()
	 * 
	 * @param vars
	 *            an array dimensioned in the number of columns, i.e.
	 *            vars.length == getNColumns(). This is not checked!
	 * @return
	 * @throws LPMatrixException
	 *             if the external solver throws an exception
	 */
	public int solve(double[] vars) throws LPMatrixException;

	/**
	 * returns the last result returned by the external solver. This method only
	 * makes sense after a call to solve() that did not throw an exception.
	 * 
	 * @return
	 */
	public int getLastSolverResult();

	/**
	 * Return the value at index row, column in the MIP matrix.
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public double getMat(int row, int column);

	/**
	 * set the value at index row, column in the matrix.
	 * 
	 * @param row
	 * @param column
	 * @param value
	 */
	public void setMat(int row, int column, double value);

	/**
	 * adjusts the value at index row, column in the matrix.
	 * 
	 * @param row
	 * @param column
	 * @param value
	 */
	public void adjustMat(int row, int column, double value);

	public void setInt(int column, boolean isInt);

	public void setUpbo(int column, double value);

	public void setLowbo(int column, double value);

	/**
	 * Sets the name of a column. Setting names is not required and no memory is
	 * reserved for column names until the first name is set on a column.
	 * 
	 * @param column
	 * @return
	 */
	public void setColName(int column, String name);

	/**
	 * Sets the name of a row. Setting names is not required and no memory is
	 * reserved for row names until the first name is set on a row.
	 * 
	 * @param row
	 * @return
	 */
	public void setRowName(int row, String name);

	public void setConstrType(int row, byte type);

	public void setMaxim();

	public void setMinim();

	public int getNrows();

	public void setRhVec(double[] rhs);

	public int getNcolumns();

	public void setRh(int row, double value);

	public void setObjective(int column, double value);

	public double getRh(int row);

	/**
	 * Gets the name of a column, or null if there is none.
	 * 
	 * @param column
	 * @return
	 */
	public String getColName(int column);

	public double getObjective(int column);

	/**
	 * Returns the column names or null if there are none.
	 * 
	 * @param column
	 * @return
	 */
	public String[] getColNames();

	public double product(double[] vars, int fromIncluding, int toExcluding, int row);

	public void setBinary(int c, boolean b);

	public int getOffset();

	/**
	 * Prints the MIP to a CSV file using ";" as a separator character. If row
	 * and column names are provided they are printed, otherwise, they are
	 * numbered
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void printLpToCSV(String fileName) throws IOException;

	/**
	 * prints the LP to the given outputWriter using the given separator
	 * character as separator. The output of this method on System.out, with
	 * "\t" as the separator returns the same representation as LpSolve's
	 * printLp() method.
	 * 
	 * @param writer
	 * @param sep
	 * @throws IOException
	 */
	public void printLp(OutputStreamWriter writer, final String sep) throws IOException;

	public String[] getRowNames();

	public byte getVarType(int c);

	public double getlowBo(int c);

	public double getUpBo(int c);

	public byte getConstrType(int r);

	public String getRowName(int r);

	public double objectiveValue(double[] vars);

}