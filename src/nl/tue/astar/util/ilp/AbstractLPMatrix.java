package nl.tue.astar.util.ilp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

abstract class AbstractLPMatrix<S> implements LPMatrix<S> {

	protected final int offset;

	// arrays in the rows
	protected final double[] rhs;
	protected final byte[] types;
	protected String[] rowNames;

	// arrays in the columns
	protected final double[] obj;
	protected final double[] upBo;
	protected final double[] lowBo;
	protected String[] colNames;
	protected final byte[] integer;

	// other variables
	protected boolean isMinimizing = true;
	protected int solverResult;

	protected AbstractLPMatrix(int rows, int columns, int offset) {
		this.offset = offset;
		rhs = new double[rows];
		types = new byte[rows];
		Arrays.fill(types, DenseLPMatrix.EQ);
		upBo = new double[columns + offset];
		Arrays.fill(upBo, INFINITE);
		lowBo = new double[columns + offset];
		obj = new double[columns + offset];
		integer = new byte[columns + offset];
		colNames = null;//new String[columns + offset];
		rowNames = null;//new String[rows];
	}

	protected AbstractLPMatrix(LPMatrix<?> matrix, int offset) {
		this.offset = offset;
		int rows = matrix.getNrows();
		int columns = matrix.getNcolumns();

		rhs = new double[rows];
		types = new byte[rows];
		for (int r = 0; r < rows; r++) {
			rhs[r] = matrix.getRh(r);
			types[r] = matrix.getConstrType(r);
		}

		upBo = new double[columns + offset];
		lowBo = new double[columns + offset];
		obj = new double[columns + offset];
		integer = new byte[columns + offset];
		for (int c = 0; c < columns; c++) {
			upBo[c + offset] = matrix.getUpBo(c);
			lowBo[c + offset] = matrix.getlowBo(c);
			obj[c + offset] = matrix.getObjective(c);
			integer[c + offset] = matrix.getVarType(c);
		}
		if (matrix.getColNames() != null) {
			colNames = new String[columns + offset];
			for (int c = 0; c < columns; c++) {
				colNames[c + offset] = new String(matrix.getColName(c));
			}
		} else {
			colNames = null;
		}
		if (matrix.getRowNames() != null) {
			rowNames = new String[rows + offset];
			for (int r = 0; r < rows; r++) {
				rowNames[r + offset] = new String(matrix.getRowName(r));
			}
		} else {
			rowNames = null;
		}
	}

	public int getLastSolverResult() {

		return solverResult;

	}

	public void setInt(int column, boolean isInt) {
		integer[column + offset] = isInt ? INTEGER : REAL;
	}

	public void setUpbo(int column, double value) {
		upBo[column + offset] = value;
	}

	public void setLowbo(int column, double value) {
		lowBo[column + offset] = value;
	}

	public void setColName(int column, String name) {
		if (colNames == null) {
			colNames = new String[getNcolumns() + offset];
		}
		colNames[column + offset] = name;
	}

	public void setRowName(int row, String name) {
		if (rowNames == null) {
			rowNames = new String[getNrows() + offset];
		}
		rowNames[row] = name;
	}

	public void setConstrType(int row, byte type) {
		types[row] = type;
	}

	public void setMaxim() {
		isMinimizing = false;
	}

	public void setMinim() {
		isMinimizing = true;
	}

	public void setRhVec(double[] rhs) {
		System.arraycopy(rhs, 0, this.rhs, 0, rhs.length);
	}

	public void setRh(int row, double value) {
		rhs[row] = value;
	}

	public void setObjective(int column, double value) {
		obj[column + offset] = value;
	}

	public double getRh(int row) {
		return rhs[row];
	}

	public String getColName(int column) {
		return colNames == null ? null : colNames[column + offset];
	}

	public double getObjective(int column) {
		return obj[column + offset];
	}

	public String[] getColNames() {
		return colNames;
	}

	public double product(double[] vars, int fromIncluding, int toExcluding, int row) {
		double r = 0;
		for (int i = fromIncluding; i < toExcluding; i++) {
			if (vars[i] != 0) {
				r += vars[i] * getMat(row, i);
			}
		}
		return r;
	}

	public double objectiveValue(double[] vars) {
		double r = 0;
		for (int c = 0; c < getNcolumns(); c++) {
			if (vars[c] != 0) {
				r += vars[c] * getObjective(c);
			}
		}
		return r;
	}

	public void setBinary(int c, boolean b) {
		this.integer[c + offset] = b ? BINARY : REAL;
	}

	public void printLp(OutputStreamWriter writer, final String sep) throws IOException {
		writer.write(sep);
		printColumnNames(writer, sep);
		writer.write(System.lineSeparator());

		writer.write(isMinimizing ? "Minimize" : "Maximize");
		writer.write(sep);
		print(obj, writer, sep);
		writer.write(System.lineSeparator());

		for (int i = 0; i < getNrows(); i++) {
			if (rowNames != null && rowNames[i] != null) {
				writer.write(rowNames[i]);
			} else {
				writer.write("R" + i);
			}
			writer.write(sep);

			print(getRow(i), writer, sep);
			if (types[i] == EQ) {
				writer.write("==");
			} else if (types[i] == LE) {
				writer.write("=<");
			} else if (types[i] == GE) {
				writer.write(">=");
			}
			writer.write(sep);

			writer.write(Double.toString(rhs[i]));
			writer.write(System.lineSeparator());
		}

		writer.write("Type");
		writer.write(sep);
		for (int i = offset; i < integer.length; i++) {
			if (integer[i] == INTEGER) {
				writer.write("Int");
			} else if (integer[i] == REAL) {
				writer.write("Real");
			} else if (integer[i] == BINARY) {
				writer.write("Bin");
			}
			writer.write(sep);
		}
		writer.write(System.lineSeparator());

		writer.write("lowBo");
		writer.write(sep);
		print(lowBo, writer, sep);
		writer.write(System.lineSeparator());

		writer.write("upBo");
		writer.write(sep);
		print(upBo, writer, sep);
		writer.write(System.lineSeparator());

	}

	protected abstract double[] getRow(int i);

	private void printColumnNames(OutputStreamWriter writer, final String sep) throws IOException {
		for (int i = offset; i < offset + getNcolumns(); i++) {
			if (colNames != null && colNames[i] != null) {
				writer.write(colNames[i]);
			} else {
				writer.write("C" + (i - offset));
			}
			writer.write(sep);
		}
	}

	private void print(double[] array, OutputStreamWriter writer, final String sep) throws IOException {
		for (int i = offset; i < array.length; i++) {
			writer.write(Double.toString(array[i]));
			writer.write(sep);
		}
	}

	public int getOffset() {
		return offset;
	}

	public void printLpToCSV(String fileName) throws IOException {
		FileWriter writer;
		writer = new FileWriter(fileName);
		printLp(writer, ";");
		writer.close();

	}

	public String[] getRowNames() {
		return rowNames;
	}

	public byte getVarType(int c) {
		return integer[c + offset];
	}

	public double getlowBo(int c) {
		return lowBo[c + offset];
	}

	public double getUpBo(int c) {
		return upBo[c + offset];
	}

	public byte getConstrType(int r) {
		return types[r];
	}

	public String getRowName(int r) {
		return rowNames[r];
	}

	/**
	 * returns the number of bytes occupied by the main arrays in this class.
	 * 
	 * @return
	 */
	public int bytesUsed() {
		return 9 * rhs.length + 8 + 25 * upBo.length + 16;
	}
}