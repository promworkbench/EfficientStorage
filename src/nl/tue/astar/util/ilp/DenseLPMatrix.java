package nl.tue.astar.util.ilp;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

abstract class DenseLPMatrix<S> extends AbstractLPMatrix<S> {

	static class LPSOLVE extends DenseLPMatrix<LpSolve> {

		public LPSOLVE(int rows, int columns) {
			super(rows, columns, 1);
		}

		public LPSOLVE(LPMatrix<?> matrix) {
			super(matrix, 1);
		}

		public LpSolve toSolver() throws LPMatrixException {
			LpSolve lp;
			try {
				synchronized (LpSolve.class) {
					lp = LpSolve.makeLp(0, getNcolumns());
				}
				lp.setAddRowmode(true);
				lp.setObjFn(obj);

				for (int r = 0; r < matrix.length; r++) {
					lp.addConstraint(matrix[r], types[r], rhs[r]);
				}
				lp.setAddRowmode(false);

				if (rowNames != null) {
					for (int r = 0; r < matrix.length; r++) {
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

				lp.setScaling(LpSolve.SCALE_GEOMETRIC | LpSolve.SCALE_EQUILIBRATE | LpSolve.SCALE_INTEGERS);
				lp.setScalelimit(5);
				lp.setPivoting(LpSolve.PRICER_DEVEX | LpSolve.PRICE_ADAPTIVE);
				lp.setMaxpivot(250);
				lp.setBbFloorfirst(LpSolve.BRANCH_AUTOMATIC);
				lp.setBbRule(LpSolve.NODE_PSEUDONONINTSELECT | LpSolve.NODE_GREEDYMODE | LpSolve.NODE_DYNAMICMODE
						| LpSolve.NODE_RCOSTFIXING);
				lp.setBbDepthlimit(-50);
				lp.setAntiDegen(LpSolve.ANTIDEGEN_FIXEDVARS | LpSolve.ANTIDEGEN_STALLING);
				lp.setImprove(LpSolve.IMPROVE_DUALFEAS | LpSolve.IMPROVE_THETAGAP);
				lp.setBasiscrash(LpSolve.CRASH_NOTHING);
				lp.setSimplextype(LpSolve.SIMPLEX_DUAL_PRIMAL);

				lp.setEpsint(1E-07);
				//				lp.printLp();
				try {
					lp.setBFPFromPath("bfp_etaPFI");
				} catch (LpSolveException e) {
					lp.setBFP(null);
				}

				return lp;
			} catch (LpSolveException e) {
				throw new LPMatrixException(e);
			}

		}

		public int solve(double[] vars) throws LPMatrixException {

			LpSolve lp = toSolver();

			try {
				solverResult = lp.solve();

				if (solverResult == LpSolve.INFEASIBLE) {
					// BVD: LpSolve has the tendency to give false infeasible answers. It's inclear when or why
					// this happens, but just in case...
					solverResult = lp.solve();

					//					lp.writeLp("D:/temp/antialignment/debugLP-Alignment.lp");
					//					lp.writeMps("D:/temp/antialignment/debugLP-Alignment.mps");

				}
				if (solverResult == LpSolve.OPTIMAL) {
					//					double[] varTmp = new double[vars.length + 1];
					lp.getVariables(vars);
					//					System.arraycopy(varTmp, offset, vars, 0, vars.length);
					return OPTIMAL;
				} else if (solverResult == LpSolve.INFEASIBLE) {
					return INFEASIBLE;
				} else {
					return OTHER;
				}

			} catch (LpSolveException e) {
				throw new LPMatrixException(e);

			} finally {

				lp.deleteAndRemoveLp();
			}
		}
	}

	static class GUROBI extends DenseLPMatrix<GRBModel> {

		private final GRBEnv env;

		public GUROBI(GRBEnv env, int rows, int columns) {
			super(rows, columns, 0);
			this.env = env;
		}

		public GUROBI(GRBEnv env, LPMatrix<?> matrix) {
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
				char sense;
				for (int i = 0; i < getNrows(); i++) {
					GRBLinExpr expr = new GRBLinExpr();
					for (int j = 0; j < getNcolumns(); j++)
						if (matrix[i][j] != 0)
							expr.addTerm(matrix[i][j], vars[j]);
					sense = types[i] == EQ ? GRB.EQUAL : types[i] == LE ? GRB.LESS_EQUAL : GRB.GREATER_EQUAL;
					model.addConstr(expr, sense, rhs[i], rowNames == null ? null : rowNames[i]);

				}
				model.update();

				return model;
			} catch (GRBException e) {
				throw new LPMatrixException(e);
			}
		}

		public int solve(double[] vars) throws LPMatrixException {
			GRBModel grbModel = toSolver();

			try {
				grbModel.optimize();
				solverResult = grbModel.get(GRB.IntAttr.Status);

				if (grbModel.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {

					for (int j = 0; j < vars.length; j++)
						vars[j] = grbModel.getVars()[j].get(GRB.DoubleAttr.X);
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

	protected final double[][] matrix;

	protected DenseLPMatrix(int rows, int columns, int offset) {
		super(rows, columns, offset);
		matrix = new double[rows][columns + offset];
	}

	protected DenseLPMatrix(LPMatrix<?> matrix, int offset) {
		super(matrix, offset);
		this.matrix = new double[matrix.getNrows()][matrix.getNcolumns() + offset];
		for (int c = 0; c < matrix.getNcolumns(); c++) {
			for (int r = 0; r < matrix.getNrows(); r++) {
				double d = matrix.getMat(r, c);
				if (d != 0) {
					this.setMat(r, c, d);
				}
			}
		}

	}

	public double getMat(int row, int column) {
		return matrix[row][column + offset];
	}

	public void setMat(int row, int column, double value) {
		matrix[row][column + offset] = value;
	}

	public void adjustMat(int row, int column, double value) {
		matrix[row][column + offset] += value;
	}

	public int getNrows() {
		return matrix.length;
	}

	public int getNcolumns() {
		return integer.length - offset;
	}

	protected double[] getRow(int row) {
		return matrix[row];
	}

	/**
	 * returns the number of bytes occupied by the main arrays in this class.
	 * 
	 * @return
	 */
	public int bytesUsed() {
		return super.bytesUsed() + matrix.length * (matrix[0].length * 8 + 4) + 4;
	}

}
