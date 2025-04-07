package nl.tue.astar.util.ilp;

import java.util.Arrays;

import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.procedure.TIntDoubleProcedure;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

abstract class SparseLPMatrix<S> extends AbstractLPMatrix<S> implements LPMatrix<S> {

	static class LPSOLVE extends SparseLPMatrix<LpSolve> {

		public LPSOLVE(int rows, int columns) throws LPMatrixException {
			super(rows, columns, 1);
		}

		public LPSOLVE(LPMatrix<?> matrix) throws LPMatrixException {
			super(matrix, 1);
		}

		public LpSolve toSolver() throws LPMatrixException {
			final LpSolve lp;
			try {
				synchronized (LpSolve.class) {
					lp = LpSolve.makeLp(0, getNcolumns());
				}
				lp.setAddRowmode(true);
				lp.setObjFn(obj);

				// Building the matrix through setMat function is considerably slower than
				// building it through the addConstr method. This however requires sorting the
				// keys.

				int[] keys = matrix.keys();
				Arrays.sort(keys);
				double[] constraint = new double[getNcolumns() + offset];
				int lastRow = -1;
				// It is essential to put the rows in the right order, i.e. from 0 to keys.length. 
				for (int i = 0; i < keys.length; i++) {
					int row = row(keys[i]);
					if (row > lastRow) {
						// new row. add Constraint
						if (lastRow != -1) {
							lp.addConstraint(constraint, types[lastRow], rhs[lastRow]);
						}
						Arrays.fill(constraint, 0);
						for (int rw = lastRow + 1; rw < row; rw++) {
							// there may be 0 rows, which need to be included. They are not part of "keys" hence,
							// we need to skip them here.
							lp.addConstraint(constraint, types[rw], rhs[rw]);
						}
						lastRow = row;
					}
					constraint[col(keys[i]) + offset] = matrix.get(keys[i]);
				}
				if (lastRow != -1) {
					lp.addConstraint(constraint, types[lastRow], rhs[lastRow]);
				}
				Arrays.fill(constraint, 0);
				for (int rw = lastRow + 1; rw < getNrows(); rw++) {
					// there may be 0 rows, which need to be included. They are not part of "keys" hence,
					// we need to skip them here.
					lp.addConstraint(constraint, types[rw], rhs[rw]);
				}

				lp.setAddRowmode(false);
				assert lp.getNrows() == getNrows();

				if (rowNames != null) {
					for (int r = 0; r < getNrows(); r++) {
						if (rowNames[r] != null) {
							lp.setRowName(r + 1, rowNames[r]);
						}
					}
				}
				for (int c = 1; c <= getNcolumns(); c++) {
					if (integer[c] == INTEGER) {
						lp.setInt(c, true);
					} else if (integer[c] == BINARY) {
						lp.setBinary(c, true);
					}
					lp.setLowbo(c, lowBo[c]);
					if (upBo[c] < INFINITE) {
						lp.setUpbo(c, upBo[c]);
					}
					if (colNames != null && colNames[c] != null) {
						lp.setColName(c, colNames[c]);
					}
				}

				if (isMinimizing) {
					lp.setMinim();
				} else {
					lp.setMaxim();
				}

				lp.setVerbose(1);

				//				lp.setScaling(LpSolve.SCALE_GEOMETRIC | LpSolve.SCALE_EQUILIBRATE | LpSolve.SCALE_INTEGERS);
				//				lp.setScalelimit(5);
				//				lp.setPivoting(LpSolve.PRICER_DEVEX | LpSolve.PRICE_ADAPTIVE);
				//				lp.setMaxpivot(250);
				//				lp.setBbFloorfirst(LpSolve.BRANCH_AUTOMATIC);
				//				lp.setBbRule(LpSolve.NODE_PSEUDONONINTSELECT | LpSolve.NODE_GREEDYMODE | LpSolve.NODE_DYNAMICMODE
				//						| LpSolve.NODE_RCOSTFIXING);
				//				lp.setBbDepthlimit(-50);
				//				lp.setAntiDegen(LpSolve.ANTIDEGEN_FIXEDVARS | LpSolve.ANTIDEGEN_STALLING);
				//				lp.setImprove(LpSolve.IMPROVE_DUALFEAS | LpSolve.IMPROVE_THETAGAP);
				//				lp.setBasiscrash(LpSolve.CRASH_NOTHING);
				//				lp.setSimplextype(LpSolve.SIMPLEX_DUAL_PRIMAL);

				//				lp.setEpsint(1E-07);
				//				try {
				//					lp.setBFPFromPath("bfp_etaPFI");
				//				} catch (LpSolveException e) {
				//					lp.setBFP(null);
				//				}

				return lp;
			} catch (LpSolveException e) {
				throw new LPMatrixException(e);
			}

		}

		private int solve(LpSolve solver) throws LpSolveException {
			return solver.solve();
		}

		public int solve(double[] vars) throws LPMatrixException {

			LpSolve lp = toSolver();

			try {
				solverResult = solve(lp);

				if (solverResult == LpSolve.INFEASIBLE || solverResult == LpSolve.NUMFAILURE) {
					// BVD: LpSolve has the tendency to give false infeasible or numfailure answers. 
					// It's unclear when or why this happens, but just in case...
					solverResult = solve(lp);

					//					lp.writeMps("D:/temp/antialignment/debugLP-Alignment.mps");

				}
				if (solverResult == LpSolve.OPTIMAL) {
					lp.getVariables(vars);
					return OPTIMAL;
				} else if (solverResult == LpSolve.INFEASIBLE) {
					return INFEASIBLE;
				} else {
					//					lp.writeLp("D:/temp/antialignment/debugLP-Alignment.lp");
					System.err.println("Error code from LpSolve solver:" + solverResult);
					return OTHER;
				}

			} catch (LpSolveException e) {
				throw new LPMatrixException(e);

			} finally {

				lp.deleteAndRemoveLp();
			}
		}

	}

	static class GUROBI extends SparseLPMatrix<GRBModel> {

		private final GRBEnv env;

		public GUROBI(GRBEnv env, int rows, int columns) throws LPMatrixException {
			super(rows, columns, 0);
			this.env = env;
		}

		public GUROBI(GRBEnv env, LPMatrix<?> matrix) throws LPMatrixException {
			super(matrix, 0);
			this.env = env;
		}

		public GRBModel toSolver() throws LPMatrixException {
			try {
				GRBModel model = new GRBModel(env);

				// Add variables to the model
				char[] vType = new char[integer.length];
				for (int v = 0; v < vType.length; v++) {
					vType[v] = integer[v] == INTEGER ? GRB.INTEGER : integer[v] == BINARY ? GRB.BINARY : GRB.CONTINUOUS;
				}

				double[] minObj = new double[obj.length];
				for (int i = 0; i < obj.length; i++) {
					if (isMinimizing) {
						minObj[i] = obj[i];
					} else {
						minObj[i] = -obj[i];
					}
				}
				GRBVar[] vars = model.addVars(lowBo, upBo, minObj, vType, colNames);
				model.update();

				// Populate A matrix

				// Build the matrix through setMat function
				GRBLinExpr[] rows = new GRBLinExpr[getNrows()];
				TIntDoubleIterator it = matrix.iterator();
				while (it.hasNext()) {
					it.advance();
					if (it.value() != 0) {
						int r = row(it.key());
						if (rows[r] == null) {
							rows[r] = new GRBLinExpr();
						}
						rows[r].addTerm(it.value(), vars[col(it.key())]);
					}
				}
				char sense;
				for (int r = 0; r < rows.length; r++) {
					sense = types[r] == EQ ? GRB.EQUAL : types[r] == LE ? GRB.LESS_EQUAL : GRB.GREATER_EQUAL;
					if (rows[r] == null) {
						// empty row. Add anyway
						rows[r] = new GRBLinExpr();
					}
					model.addConstr(rows[r], sense, rhs[r], rowNames == null ? null : rowNames[r]);
				}
				model.update();

				return model;
			} catch (GRBException e) {
				throw new LPMatrixException(e);
			}
		}

		public int solve(double[] vars) throws LPMatrixException {

			//			long t1 = System.currentTimeMillis();

			GRBModel grbModel = toSolver();
			//			long t2 = System.currentTimeMillis();

			try {
				grbModel.optimize();
				solverResult = grbModel.get(GRB.IntAttr.Status);
				//				long t3 = System.currentTimeMillis();

				//				System.out.println("vars: " + nRows + " cols: " + nColumns + " setup: " + (t2 - t1) + " vs solve: "
				//						+ (t3 - t2));

				if (grbModel.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
					GRBVar[] grbVars = grbModel.getVars();
					for (int j = 0; j < vars.length; j++)
						vars[j] = grbVars[j].get(GRB.DoubleAttr.X);
					return OPTIMAL;
				} else if (grbModel.get(GRB.IntAttr.Status) == GRB.Status.INFEASIBLE
						|| grbModel.get(GRB.IntAttr.Status) == GRB.Status.INF_OR_UNBD) {
					return INFEASIBLE;

				} else {
					return OTHER;
				}

			} catch (GRBException e) {
				throw new LPMatrixException(e);

			} finally {
				grbModel.dispose();
			}
		}

	}

	private static final class TIntDoubleHashMapWithCapacity extends TIntDoubleHashMap {

		public TIntDoubleHashMapWithCapacity(int initialCapacity) {
			super(initialCapacity);
		}

		public int bytesUsed() {
			return super.capacity() * 9 + 8;
		}

	};

	protected final TIntDoubleHashMapWithCapacity matrix;

	protected final int nRows;
	protected final int nColumns;

	protected SparseLPMatrix(int rows, int columns, int offset) throws LPMatrixException {
		super(rows, columns, offset);
		this.nRows = rows;
		this.nColumns = columns;
		if (rows > 0x0000FFFF || columns > 0x0000FFFF) {
			throw new LPMatrixException("Sparse matrix cannot contain more than " + 0x0000FFFF + " rows or columns.");
		}
		this.matrix = new TIntDoubleHashMapWithCapacity(rows + columns);

	}

	protected SparseLPMatrix(LPMatrix<?> matrix, int offset) throws LPMatrixException {
		super(matrix, offset);
		this.nRows = matrix.getNrows();
		this.nColumns = matrix.getNcolumns();
		if (nRows > 0x0000FFFF || nColumns > 0x0000FFFF) {
			throw new LPMatrixException("Sparse matrix cannot contain more than " + 0x0000FFFF + " rows or columns.");
		}
		this.matrix = new TIntDoubleHashMapWithCapacity(nRows + nColumns);
		for (int c = 0; c < nColumns; c++) {
			for (int r = 0; r < nRows; r++) {
				double d = matrix.getMat(r, c);
				if (d != 0) {
					this.setMat(r, c, d);
				}
			}
		}
	}

	protected int toInt(int row, int column) {
		return (row << 16) | (column & 0x0000FFFF);
	}

	protected int row(int val) {
		return val >>> 16;
	}

	protected int col(int val) {
		return val & 0x0000FFFF;
	}

	public double getMat(int row, int column) {
		return matrix.get(toInt(row, column));
	}

	public void setMat(int row, int column, double value) {
		matrix.put(toInt(row, column), value);
	}

	public void adjustMat(int row, int column, double value) {
		matrix.adjustOrPutValue(toInt(row, column), value, value);
	}

	public int getNrows() {
		return nRows;
	}

	public int getNcolumns() {
		return nColumns;
	}

	/**
	 * The method getRow() is rather expensive as it really instantiates a row and
	 * needs to search through the entire matrix for elements in that row, i.e. the
	 * method call or O(N) where N is the number of non-zero values in the matrix.
	 * Note that changes to this row are NOT backed up in the underlying matrix.
	 */
	protected double[] getRow(final int row) {
		final double[] rowArray = new double[getNcolumns() + offset];
		matrix.forEachEntry(new TIntDoubleProcedure() {

			public boolean execute(int key, double val) {
				if (val != 0 && row(key) == row) {
					rowArray[col(key) + offset] = val;
				}
				return true;
			}
		});
		return rowArray;
	}

	/**
	 * returns the number of bytes occupied by the main arrays in this class.
	 * 
	 * @return
	 */
	public int bytesUsed() {
		return super.bytesUsed() + matrix.bytesUsed();
	}

}
