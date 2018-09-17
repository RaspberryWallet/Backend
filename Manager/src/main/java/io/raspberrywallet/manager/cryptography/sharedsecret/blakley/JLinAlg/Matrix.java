package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.io.Serializable;

/**
 * This class represents a matrix.
 * 
 * @author Andreas Keilhauer, Simon D. Levy
 */

public class Matrix implements Serializable {

	protected int numOfRows;

	protected int numOfCols;

	protected FieldElement[][] entries;

	/**
	 * Constructs an empty Matrix with a certain number of rows and columns.
	 * 
	 * @param numberOfRows
	 * @param numberOfCols
	 */

	public Matrix(int numberOfRows, int numberOfCols) {
		this.numOfRows = numberOfRows;
		this.numOfCols = numberOfCols;
		this.entries = new FieldElement[numberOfRows][numberOfCols];

	}

	/**
	 * Constructs a Matrix with a certain number of rows and columns and and
	 * fills it with FieldElements from one FieldElement array.
	 * 
	 * @param numberOfRows
	 * @param theEntries
	 *            in a FieldElement array.
	 * @throws InvalidOperationException
	 *             if theEntries is null
	 * @throws InvalidOperationException
	 *             if numberOfRows is not valid
	 */

	public Matrix(FieldElement[] theEntries, int numberOfRows)
			throws InvalidOperationException {
		if (theEntries == null) {
			throw new InvalidOperationException(
					"Tried to construct matrix with a null entry array");
		}
		if (theEntries.length % numberOfRows != 0) {
			throw new InvalidOperationException(
					"Tried to construct matrix with " + theEntries.length
							+ " entries and " + numberOfRows + " rows");

		}
		this.numOfRows = numberOfRows;
		this.numOfCols = theEntries.length / numberOfRows;
		this.entries = new FieldElement[this.numOfRows][this.numOfCols];
		for (int i = 0; i < this.numOfRows; i++) {
			for (int j = 0; j < this.numOfCols; j++) {
				entries[i][j] = theEntries[i * this.numOfCols + j];
			}
		}
	}

	/**
	 * Constructs a Matrix out of an array of row vectors.
	 * 
	 * @param rowVectors
	 *            as an array of Vectors
	 * @throws InvalidOperationException
	 *             if rowVectors is null
	 * @throws InvalidOperationException
	 *             if rowVectors contains a null Vector
	 * @throws InvalidOperationException
	 *             if rowVectors contains Vectors of unequal lengths
	 */

	public Matrix(Vector[] rowVectors) throws InvalidOperationException {
		if (rowVectors == null) {
			throw new InvalidOperationException(
					"Tried to construct matrix but array of row vectors was null");
		}
		for (int i = 0; i < rowVectors.length; i++) {
			if (rowVectors[i] == null) {
				throw new InvalidOperationException(
						"Tried to construct matrix and found null-vector");
			}
		}
		int vectorLength = rowVectors[0].length();
		for (int i = 0; i < rowVectors.length; i++) {
			if (rowVectors[i].length() != vectorLength) {
				throw new InvalidOperationException(
						"Tried to construct matrix but not all vectors"
								+ " had the same length");
			}
		}

		numOfRows = rowVectors.length;
		numOfCols = vectorLength;
		entries = new FieldElement[numOfRows][numOfCols];

		for (int k = 0; k < numOfRows; k++) {
			for (int l = 0; l < numOfCols; l++) {
				entries[k][l] = rowVectors[k].getEntry(l + 1);
			}
		}
	}

	/**
	 * Constructs a Matrix out of a two dimensional array of FieldElements.
	 * 
	 * @param theEntries
	 *            as a two dimensional FieldElement array.
	 * @throws InvalidOperationException
	 *             if theEntries is null
	 */

	public Matrix(FieldElement[][] theEntries) throws InvalidOperationException {
		if (theEntries == null) {
			throw new InvalidOperationException(
					"Tried to construct matrix but entry array was null");
		}
		this.numOfRows = theEntries.length;
		this.numOfCols = theEntries[0].length;

		this.entries = theEntries;
	}

	/**
	 * Constructs a Matrix out of a two dimensional array of FieldElements. But
	 * with the dimensions given it is faster than the constructor above because
	 * it doesnt check any integrity of the entries-array.
	 * 
	 * @param theEntries
	 * @param rows
	 * @param cols
	 */

	public Matrix(FieldElement[][] theEntries, int rows, int cols) {
		this.numOfRows = rows;
		this.numOfCols = cols;
		this.entries = theEntries;
	}

	/**
	 * Gets the number of rows of this Matrix.
	 * 
	 * @return number of rows
	 */

	public int getRows() {
		return numOfRows;
	}

	/**
	 * Gets the number of columns of this Matrix.
	 * 
	 * @return number of columns
	 */

	public int getCols() {
		return numOfCols;
	}

	public FieldElement[][] getEntries() {
		return this.entries;
	}

	/**
	 * Gets the entry of this Matrix at a certain row - and col index.
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @return the FieldElement at this row - and column index
	 * @throws InvalidOperationException
	 *             if rowIndex is not between 1 and numberOfRows or colIndex is
	 *             not between 1 and numberOfCols
	 */

	public FieldElement get(int rowIndex, int colIndex)
			throws InvalidOperationException {
		FieldElement returnValue = null;
		try {
			returnValue = entries[rowIndex - 1][colIndex - 1];
		} catch (ArrayIndexOutOfBoundsException e) {
			if (rowIndex > this.numOfRows || rowIndex < 1) {
				throw new InvalidOperationException("Tried row index "
						+ rowIndex + ". Only row indices from 1 to "
						+ this.numOfRows + " valid");
			} else {
				throw new InvalidOperationException("Tried column index "
						+ colIndex + ". Only column indices " + "from 1 to "
						+ this.numOfCols + " valid");
			}
		}
		return returnValue;
	}

	/**
	 * Sets the entry of this Matrix at a certain row - and col index.
	 * 
	 * @param rowIndex
	 * @param colIndex
	 * @param newEntry
	 * @throws InvalidOperationException
	 *             if rowIndex or colIndex is invalid
	 */

	public void set(int rowIndex, int colIndex, FieldElement newEntry)
			throws InvalidOperationException {
		try {
			entries[rowIndex - 1][colIndex - 1] = newEntry;
		} catch (ArrayIndexOutOfBoundsException e) {
			if (rowIndex > this.numOfRows || rowIndex < 1) {
				throw new InvalidOperationException("Tried row index "
						+ rowIndex + ". Only row indices from 1 to "
						+ this.numOfRows + " valid");
			} else {
				throw new InvalidOperationException("Tried column index "
						+ colIndex + ". Only column indices " + "from 1 to "
						+ this.numOfCols + " valid");
			}
		}
	}

	/**
	 * Gets the row vector at a certain row index.
	 * 
	 * @param rowIndex
	 */
	public Vector getRow(int rowIndex) {
		FieldElement[] rowEntries = new FieldElement[numOfCols];
		for (int i = 0; i < numOfCols; i++) {
			try {
				rowEntries[i] = entries[rowIndex - 1][i];
			} catch (ArrayIndexOutOfBoundsException a) {
				throw new InvalidOperationException("Tried row index "
						+ rowIndex + ". Only row indices from 1 to "
						+ this.numOfRows + " valid");
			}
		}
		return new Vector(rowEntries);
	}

	/**
	 * Gets the column vector at a certain column index.
	 * 
	 * @param colIndex
	 */

	public Vector getCol(int colIndex) {
		FieldElement[] colEntries = new FieldElement[numOfRows];
		for (int i = 0; i < numOfRows; i++) {
			try {
				colEntries[i] = entries[i][colIndex - 1];
			} catch (ArrayIndexOutOfBoundsException a) {
				throw new InvalidOperationException("Tried row index "
						+ colIndex + ". Only row indices from 1 to "
						+ this.numOfCols + " valid");
			}

		}
		return new Vector(colEntries);
	}

	/**
	 * Sets the row vector at a certain index of the matrix.
	 * 
	 * @param rowIndex
	 * @param vector
	 * @throws InvalidOperationException
	 *             if index out of bounds
	 */

	public void setRow(int rowIndex, Vector vector)
			throws InvalidOperationException {

		if (this.numOfCols != vector.length()) {
			throw new InvalidOperationException("Tried to set row number "
					+ rowIndex + "of a matrix with " + this.numOfCols
					+ " columns with a vector having length " + vector.length()
					+ " ");
		}

		for (int i = 1; i <= vector.length(); i++) {
			set(rowIndex, i, vector.getEntry(i));
		}
	}

	/**
	 * Sets the column vector at a certain column index of the matrix.
	 * 
	 * @param colIndex
	 * @param vector
	 */

	public void setCol(int colIndex, Vector vector) {

		if (this.numOfRows != vector.length()) {
			throw new InvalidOperationException("Tried to set column number "
					+ colIndex + "of a matrix with " + this.numOfRows
					+ " rows with a vector having length " + vector.length()
					+ " ");
		}

		for (int i = 1; i <= vector.length(); i++) {
			set(i, colIndex, vector.getEntry(i));
		}
	}

	/**
	 * Returns this Matrix without the row at a certain row index.
	 * 
	 * @param rowIndex
	 * @return the matrix without the row at the row index.
	 */

	public Matrix withoutRow(int rowIndex) {
		// Exception still missing here
		Matrix tmp = new Matrix(this.getRows() - 1, this.getCols());
		int counter = 0;
		for (int i = 1; i <= this.getRows(); i++) {
			counter++;
			if (i == rowIndex) {
				counter--;
				continue;
			}
			tmp.setRow(counter, this.getRow(i));
		}
		return tmp;
	}

	/**
	 * Returns this Matrix without the column at a certain column index.
	 * 
	 * @param colIndex
	 * @return the matrix without the column at the column index.
	 */

	public Matrix withoutCol(int colIndex) {
		// Exception still missing here
		Matrix tmp = new Matrix(this.getRows(), this.getCols() - 1);
		int counter = 0;
		for (int i = 1; i <= this.getCols(); i++) {
			counter++;
			if (i == colIndex) {
				counter--;
				continue;
			}
			tmp.setCol(counter, this.getCol(i));
		}
		return tmp;
	}

	/**
	 * Returns a Matrix with a Vector inserted at specified index as a column.
	 * Index 1 will it put at the beginning and index numOfCols+1 will attach it
	 * to the end.
	 * 
	 * @param vector
	 * @param colIndex
	 * @return matrix with vector inserted.
	 * @throws InvalidOperationException
	 *             if index out of bounds
	 */

	public Matrix insertCol(int colIndex, Vector vector)
			throws InvalidOperationException {

		if (this.getRows() != vector.length()) {
			throw new InvalidOperationException("This vector\n" + vector
					+ "\n cannot be attached to " + "the Matrix\n" + this
					+ " as a column vector. The length does " + "not match");
		}

		if (colIndex < 1 || colIndex > this.getCols() + 1) {
			throw new InvalidOperationException(colIndex
					+ " is not a valid column index for inserting " + vector
					+ " into\n" + this);
		}

		Matrix tmp = new Matrix(this.getRows(), this.getCols() + 1);
		int colOffset = 0;
		for (int col = 1; col <= tmp.getCols(); col++) {
			if (col == colIndex) {
				tmp.setCol(col, vector);
				colOffset = 1;
			} else {
				tmp.setCol(col, this.getCol(col - colOffset));
			}
		}

		return tmp;
	}

	/**
	 * Returns a Matrix with a Vector inserted at specified index as a row.
	 * Index 1 will put at the beginning and index numOfRows+1 will attach it to
	 * the end.
	 * 
	 * @param vector
	 * @param rowIndex
	 * @return matrix with vector inserted.
	 * @throws InvalidOperationException
	 *             if index out of bounds
	 */

	public Matrix insertRow(int rowIndex, Vector vector)
			throws InvalidOperationException {
		if (this.getCols() != vector.length()) {
			throw new InvalidOperationException("This vector\n" + vector
					+ "\n cannot be inserted into the " + "Matrix\n" + this
					+ " as a row vector");
		}

		if (rowIndex < 1 || rowIndex > this.getRows() + 1) {
			throw new InvalidOperationException(rowIndex
					+ " is not a valid row index for inserting " + vector
					+ " into\n" + this);
		}

		Matrix tmp = new Matrix(this.getRows() + 1, this.getCols());
		int rowOffset = 0;
		for (int row = 1; row <= tmp.getRows(); row++) {
			if (row == rowIndex) {
				tmp.setRow(row, vector);
				rowOffset = 1;
			} else {
				tmp.setRow(row, this.getRow(row - rowOffset));
			}
		}
		return tmp;
	}

	/**
	 * Get a submatrix.
	 * 
	 * @param i0
	 *            Initial row index
	 * @param i1
	 *            Final row index
	 * @param j0
	 *            Initial column index
	 * @param j1
	 *            Final column index
	 * @return A(i0:i1,j0:j1)
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */

	public Matrix getMatrix(int i0, int i1, int j0, int j1) {
		i0--;
		i1--;
		j0--;
		j1--;
		Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
		FieldElement[][] B = X.getEntries();
		try {
			for (int i = i0; i <= i1; i++) {
				for (int j = j0; j <= j1; j++) {
					B[i - i0][j - j0] = entries[i][j];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
		return X;
	}

	/**
	 * Get a submatrix.
	 * 
	 * @param r
	 *            Array of row indices.
	 * @param j0
	 *            Initial column index
	 * @param j1
	 *            Final column index
	 * @return A(r(:),j0:j1)
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */

	public Matrix getMatrix(int[] r, int j0, int j1) {
		Matrix X = new Matrix(r.length, j1 - j0 + 1);
		FieldElement[][] B = X.getEntries();
		try {
			for (int i = 0; i < r.length; i++) {
				for (int j = j0; j <= j1; j++) {
					B[i][j - j0] = entries[r[i]][j];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
		return X;
	}

	/**
	 * Returns a String representation of this Matrix.
	 * 
	 * @return String representation
	 */

	public String toString() {
		String tempString = "";
		for (int i = 1; i < numOfRows; i++) {
			tempString += getRow(i).toString() + "\n";
		}
		return tempString + getRow(this.getRows()).toString();
	}

	/**
	 * Returns the matrix that is the sum of this Matrix and another matrix.
	 * 
	 * @param anotherMatrix
	 * @return this + anotherMatrix
	 * @throws InvalidOperationException
	 *             if matrices differ in size
	 */

	public Matrix add(Matrix anotherMatrix) throws InvalidOperationException {
		FieldElement[][] anotherMatrixEntries = this.getEntries();
		FieldElement[][] resultMatrixEntries = new FieldElement[this.numOfRows][this.numOfCols];
		for (int row = 0; row < this.numOfRows; row++) {
			for (int col = 0; col < this.numOfCols; col++) {
				resultMatrixEntries[row][col] = entries[row][col]
						.add(anotherMatrixEntries[row][col]);
			}
		}
		return new Matrix(entries);
	}

	/**
	 * Returns the matrix that is the sum of this Matrix and a scalar.
	 * 
	 * @param scalar
	 * @return this + scalar
	 */

	public Matrix add(FieldElement scalar) {

		return operate(this, scalar, new AddOperator());
	}

	/**
	 * Returns the matrix that is this this Matrix minus another one.
	 * 
	 * @param anotherMatrix
	 * @return this - anotherMatrix
	 * @throws InvalidOperationException
	 *             if matrices differ in size
	 */

	public Matrix subtract(Matrix anotherMatrix)
			throws InvalidOperationException {
		FieldElement[][] anotherMatrixEntries = this.getEntries();
		FieldElement[][] resultMatrixEntries = new FieldElement[this.numOfRows][this.numOfCols];
		for (int row = 0; row < this.numOfRows; row++) {
			for (int col = 0; col < this.numOfCols; col++) {
				resultMatrixEntries[row][col] = entries[row][col]
						.subtract(anotherMatrixEntries[row][col]);
			}
		}
		return new Matrix(entries);
	}

	/**
	 * Returns the matrix that is this this Matrix minus a scalar.
	 * 
	 * @param scalar
	 * @return this - scalar
	 */

	public Matrix subtract(FieldElement scalar) {
		return operate(this, scalar, new SubtractOperator());
	}

	/**
	 * Returns a Matrix that is this Matrix multiplied with a scalar.
	 * 
	 * @param scalar
	 * @return multiplied Matrix
	 */

	public Matrix multiply(FieldElement scalar) {

		return operate(this, scalar, new MultiplyOperator());
	}

	/**
	 * Returns a Matrix that is this Matrix divided by a scalar.
	 * 
	 * @param scalar
	 * @return divided Matrix
	 */

	public Matrix divide(FieldElement scalar) {

		return operate(this, scalar, new DivideOperator());
	}

	/**
	 * Returns the vector that is the product of this Matrix and a given vector.
	 * 
	 * @param vector
	 * @return product vector
	 * @throws InvalidOperationException
	 *             if number of columns of this matrix does not equal number of
	 *             elements of vector
	 */

	public Vector multiply(Vector vector) throws InvalidOperationException {
		if (this.numOfCols != vector.length()) {
			String err = "Tried to multiply \n" + this + " and \n" + vector
					+ "Not correct format!";
			throw new InvalidOperationException(err);
		}

		Vector resultVector = new Vector(this.numOfRows);
		for (int i = 1; i <= numOfRows; i++) {
			resultVector.set(i, this.getRow(i).multiply(vector));
		}

		return resultVector;
	}

	/**
	 * Returns the matrix that is the product of this Matrix and another matrix.
	 * 
	 * @param anotherMatrix
	 * @return product matrix
	 * @throws InvalidOperationException
	 *             if number of columns of this matrix does not equal number of
	 *             rows of the other matrix
	 */

	public Matrix multiply(Matrix anotherMatrix)
			throws InvalidOperationException {
		return MatrixMultiplication.simple(this, anotherMatrix);
	}

	/**
	 * Returns a deep copy of this Matrix.
	 * 
	 * @return matrix copy
	 */

	public Matrix copy() {
		Matrix tmp = new Matrix(this.getRows(), this.getCols());
		for (int row = 1; row <= this.getRows(); row++) {
			for (int col = 1; col <= this.getCols(); col++) {
				tmp.set(row, col, this.get(row, col));
			}
		}
		return tmp;
	}

	/**
	 * Returns the determinant of this Matrix.
	 * 
	 * @return determinant
	 * @throws InvalidOperationException
	 *             if matrix is not square
	 */

	public FieldElement det() throws InvalidOperationException {
		if (this.getRows() != this.getCols()) {
			throw new InvalidOperationException(
					"Sqare matrix needed for determinant");
		}
		return detCalc();
	}

	private FieldElement detCalc() {
		Matrix tmp = this.gausselim();
		FieldElement determinant = (tmp.get(1, 1).one());

		for (int row = 1; row <= tmp.getRows(); row++) {
			determinant = determinant.multiply(tmp.get(row, row));
		}

		return determinant;
	}

	/**
	 * Swaps two rows of this Matrix.
	 * 
	 * @param rowIndex1
	 *            index of first swap partner.
	 * @param rowIndex2
	 *            index of second swap partner.
	 */

	public void swapRows(int rowIndex1, int rowIndex2) {
		Vector tmp = this.getRow(rowIndex1);
		this.setRow(rowIndex1, this.getRow(rowIndex2));
		this.setRow(rowIndex2, tmp);
	}

	/**
	 * Swaps two columns of this Matrix.
	 * 
	 * @param colIndex1
	 *            index of first swap partner.
	 * @param colIndex2
	 *            index of second swap partner.
	 */

	public void swapCols(int colIndex1, int colIndex2) {
		Vector tmp = this.getCol(colIndex1);
		this.setCol(colIndex1, this.getCol(colIndex2));
		this.setCol(colIndex2, tmp);
	}

	/**
	 * Returns a matrix that is this Matrix with the Gauss-Jordan algorithm
	 * executed on. In other words: It returns the reduced row echelon form of
	 * this Matrix.
	 * 
	 * @return matrix in reduced row-echelon form
	 */

	public Matrix gaussjord() {
		Matrix tmp = this.copy();

		int minOfRowsCols = Math.min(tmp.getRows(), tmp.getCols());
		int colCounter = 0;

		int row = 0;
		while (row < minOfRowsCols && colCounter < tmp.getCols()) {
			row++;
			colCounter++;
			FieldElement diagonalEntry = tmp.get(row, colCounter);

			if (diagonalEntry.isZero()) {
				// search for non zero entry
				boolean found = false;
				for (int candidate = row + 1; candidate <= tmp.getRows(); candidate++) {
					if (!tmp.get(candidate, colCounter).isZero()) {
						tmp.swapRows(row, candidate);
						found = true;
						break;
					}
				}

				if (!found) {
					if (colCounter == tmp.getCols()) {
						return tmp;
					}

					row--;
					continue;
				} else {
					diagonalEntry = tmp.get(row, colCounter);
				}
			}

			for (int j = colCounter; j <= tmp.getCols(); j++) {
				FieldElement oldEntry = tmp.get(row, j);
				tmp.set(row, j, oldEntry.divide(diagonalEntry));
			}

			for (int j = 1; j <= tmp.getRows(); j++) {

				FieldElement factor = tmp.get(j, colCounter);
				if (row == j || factor.isZero()) {
					continue;
				}

				for (int k = colCounter; k <= tmp.getCols(); k++) {
					FieldElement oldEntry = tmp.get(j, k);
					tmp.set(j, k, oldEntry.subtract(tmp.get(row, k).multiply(
							factor)));
				}
			}
		}
		return tmp;
	}

	/**
	 * Returns a matrix that is this Matrix with Gauss-elimination executed on.
	 * In other words: It returns a row echelon form of this Matrix.
	 * 
	 * @return matrix in row-echelon form
	 */

	public Matrix gausselim() {
		Matrix tmp = this.copy();

		int minOfRowsCols = Math.min(tmp.getRows(), tmp.getCols());
		int colCounter = 0;

		int row = 0;
		while (row < minOfRowsCols && colCounter < tmp.getCols()) {
			row++;
			colCounter++;
			FieldElement diagonalEntry = tmp.get(row, colCounter);

			if (diagonalEntry.isZero()) {
				// search for non zero entry
				boolean found = false;
				for (int candidate = row + 1; candidate <= tmp.getRows(); candidate++) {
					if (!tmp.get(candidate, colCounter).isZero()) {
						tmp.swapRows(row, candidate);
						found = true;
						break;
					}
				}

				if (!found) {
					if (colCounter == tmp.getCols()) {
						return tmp;
					}

					row--;
					continue;
				} else {
					diagonalEntry = tmp.get(row, colCounter);
				}
			}

			for (int j = row; j <= tmp.getRows(); j++) {

				FieldElement factor = tmp.get(j, colCounter).divide(
						diagonalEntry);
				if (row == j || factor.isZero()) {
					continue;
				}

				for (int k = colCounter; k <= tmp.getCols(); k++) {
					FieldElement oldEntry = tmp.get(j, k);
					tmp.set(j, k, oldEntry.subtract(factor.multiply(tmp.get(
							row, k))));
				}
			}
		}
		return tmp;
	}

	/**
	 * Calculates the eigenvalues of a matrix.
	 * 
	 * @return All eigenvalues as a Vector with Complex entries.
	 * @throws InvalidOperationException
	 *             if this matrix is no square matrix or the entries are not all
	 *             DoubleWrappers.
	 */

	public Vector eig() throws InvalidOperationException {
		return Handbook.eig(this);
	}

	/**
	 * Returns whether the row at the specified row index is a zero row or not.
	 * 
	 * @param rowIndex
	 *            index of the row to be tested
	 * @return true if there are only zero elements in the row.
	 */

	public boolean isZeroRow(int rowIndex) {
		for (int col = 1; col <= this.getCols(); col++) {
			if (!this.get(rowIndex, col).isZero()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether the column at the specified column index is a zero column
	 * or not.
	 * 
	 * @param colIndex
	 *            of the column to be tested
	 * @return true if there are only zero elements in the column.
	 */

	public boolean isZeroCol(int colIndex) {
		for (int row = 1; row <= this.getRows(); row++) {
			if (!this.get(row, colIndex).isZero()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the rank of this Matrix.
	 * 
	 * @return rank
	 */

	public int rank() {
		Matrix tmp = this.gausselim();
		int numberOfZeroRows = 0;
		int row = tmp.getRows();

		while (row > 0 && tmp.isZeroRow(row)) {
			numberOfZeroRows++;
			row--;
		}

		return tmp.getRows() - numberOfZeroRows;
	}

	/**
	 * Returns a matrix that is this Matrix transposed.
	 * 
	 * @return transposed matrix
	 */

	public Matrix transpose() {
		Matrix tmp = new Matrix(this.getCols(), this.getRows());
		for (int row = 1; row <= this.getRows(); row++) {
			for (int col = 1; col <= this.getCols(); col++) {
				tmp.set(col, row, this.get(row, col));
			}
		}
		return tmp;
	}

	/**
	 * Returns a matrix that is this Matrix hermitianly transposed. For almost
	 * all matrices this method is equivalent to transpose. But in case of
	 * complex number entries the matrix will be transposed and all entries will
	 * be conjugated as well.
	 * 
	 * @return hermitianly transposed matrix
	 */

	public Matrix hermitian() {
		Matrix tmp = new Matrix(this.getCols(), this.getRows());
		for (int row = 1; row <= this.getRows(); row++) {
			for (int col = 1; col <= this.getCols(); col++) {
				FieldElement test = this.get(row, col);
				if (test instanceof Complex) {
					Complex el = (Complex) test;
					tmp.set(col, row, el.conjugate());
				} else {
					tmp.set(col, row, test);
				}

			}
		}
		return tmp;
	}

	/**
	 * Tests two matrices for equality.
	 * 
	 * @return true if and only if the two matrices equal in all entries.
	 * @param anotherMatrix
	 */

	public boolean equals(Matrix anotherMatrix) {
		if (this.getRows() == anotherMatrix.getRows()
				&& this.getCols() == anotherMatrix.getCols()) {
			for (int row = 1; row <= this.getRows(); row++) {
				for (int col = 1; col <= this.getCols(); col++) {
					if (!this.get(row, col).equals(anotherMatrix.get(row, col))) {
						return false;
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Returns the inverse of this Matrix.
	 * 
	 * @return inverse Matrix
	 */

	public Matrix inverse() {
		if (this.getRows() != this.getCols()) {
			return null;
		}

		Matrix tmp = this.copy();

		FieldElement zero = tmp.get(1, 1).zero();
		FieldElement one = zero.one();

		FieldElement[][] entries2 = new FieldElement[tmp.getRows()][tmp
				.getCols()];

		for (int i = 0; i < tmp.getRows(); i++) {
			for (int j = 0; j < tmp.getCols(); j++) {
				if (i == j) {
					entries2[i][j] = one;
				} else {
					entries2[i][j] = zero;
				}
			}
		}

		Matrix tmp2 = new Matrix(entries2);

		int minOfRowsCols = Math.min(tmp.getRows(), tmp.getCols());
		int colCounter = 0;

		int row = 0;
		while (row < minOfRowsCols && colCounter < tmp.getCols()) {
			row++;
			colCounter++;
			FieldElement diagonalEntry = tmp.get(row, colCounter);

			if (diagonalEntry.isZero()) {
				// search for non zero entry
				boolean found = false;
				for (int candidate = row + 1; candidate <= tmp.getRows(); candidate++) {
					if (!tmp.get(candidate, colCounter).isZero()) {
						tmp.swapRows(row, candidate);
						tmp2.swapRows(row, candidate);
						found = true;
						break;
					}
				}

				if (!found) {
					if (colCounter == tmp.getCols()) {
						return null; // Because there is no inverse of this.
					}

					row--;
					continue;
				} else {
					diagonalEntry = tmp.get(row, colCounter);

				}
			}

			for (int j = 1; j <= tmp.getCols(); j++) {
				FieldElement oldEntry = tmp.get(row, j);
				FieldElement oldEntry2 = tmp2.get(row, j);
				tmp.set(row, j, oldEntry.divide(diagonalEntry));
				tmp2.set(row, j, oldEntry2.divide(diagonalEntry));
			}

			for (int j = 1; j <= tmp.getRows(); j++) {

				FieldElement factor = tmp.get(j, colCounter);
				if (row == j || factor.isZero()) {
					continue;
				}

				for (int k = 1; k <= tmp.getCols(); k++) {
					FieldElement oldEntry = tmp.get(j, k);
					FieldElement oldEntry2 = tmp2.get(j, k);
					tmp.set(j, k, oldEntry.subtract(tmp.get(row, k).multiply(
							factor)));
					tmp2.set(j, k, oldEntry2.subtract(tmp2.get(row, k)
							.multiply(factor)));
				}
			}
		}

		return tmp2;
	}

	/**
	 * Divides this Matrix by a scalar.
	 * 
	 * @param scalar
	 */

	public void divideReplace(FieldElement scalar) {

		operate(scalar, new DivideOperator());
	}

	/**
	 * Divides this Matrix by another.
	 * 
	 * @param anotherMatrix
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */

	public void divideReplace(Matrix anotherMatrix)
			throws InvalidOperationException {

		operate(anotherMatrix, new DivideOperator(), "divide");
	}

	/**
	 * Multiplies this Matrix by a scalar.
	 * 
	 * @param scalar
	 */

	public void multiplyReplace(FieldElement scalar) {

		operate(scalar, new MultiplyOperator());
	}

	/**
	 * Multiplies this Matrix element-wise by another.
	 * 
	 * @param anotherMatrix
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */

	public void multiplyReplace(Matrix anotherMatrix)
			throws InvalidOperationException {

		operate(anotherMatrix, new MultiplyOperator(), "multiply");
	}

	/**
	 * Adds a scalar to this Matrix.
	 * 
	 * @param scalar
	 */

	public void addReplace(FieldElement scalar) {

		operate(scalar, new AddOperator());
	}

	/**
	 * Adds another matrix to this Matrix.
	 * 
	 * @param anotherMatrix
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */

	public void addReplace(Matrix anotherMatrix) {

		operate(anotherMatrix, new AddOperator(), "add");
	}

	/**
	 * Subtracts a scalar from this Matrix.
	 * 
	 * @param scalar
	 */

	public void subtractReplace(FieldElement scalar) {

		operate(scalar, new SubtractOperator());
	}

	/**
	 * Subtracts another Matrix from this.
	 * 
	 * @param anotherMatrix
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */

	public void subtractReplace(Matrix anotherMatrix) {

		operate(anotherMatrix, new SubtractOperator(), "subtract");
	}

	/**
	 * Returns the logical AND of this Matrix with another. Elements of the
	 * result are 1 where both matrices are non-zero, and zero elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of 1's and 0's
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */

	public Matrix and(Matrix anotherMatrix) {

		return operate(this, anotherMatrix, new AndOperator(), "AND");
	}

	/**
	 * Returns the logical OR of this Matrix with another. Elements of the
	 * result are 1 where both matrices are non-zero, and zero elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of 1's and 0's
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */

	public Matrix or(Matrix anotherMatrix) {

		return operate(this, anotherMatrix, new OrOperator(), "OR");
	}

	/**
	 * Returns the logical negation of this Matrix. Elements of the result are 1
	 * where the matrix is zero, and one elsewhere.
	 * 
	 * @return Matrix of 1's and 0's
	 */

	public Matrix not() {

		return this.apply(new NotOperator());
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are less
	 * than those of another Matrices, and zeros elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of ones and zeros
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */
	public Matrix lt(Matrix anotherMatrix) {

		return comparison(anotherMatrix, new LessThanComparator(), "LT");
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are less
	 * than a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Matrix of ones and zeros
	 */
	public Matrix lt(FieldElement scalar) {

		return comparison(scalar, new LessThanComparator());
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are less
	 * than or equal to those of another Matrices, and zeros elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of ones and zeros
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */
	public Matrix le(Matrix anotherMatrix) {

		return comparison(anotherMatrix, new LessThanOrEqualToComparator(),
				"LE");
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are less
	 * than or equal to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Matrix of ones and zeros
	 */
	public Matrix le(FieldElement scalar) {

		return comparison(scalar, new LessThanOrEqualToComparator());
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are greater
	 * than those of another Matrices, and zeros elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of ones and zeros
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */
	public Matrix gt(Matrix anotherMatrix) {

		return comparison(anotherMatrix, new GreaterThanComparator(), "GT");
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are greater
	 * than a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Matrix of ones and zeros
	 */
	public Matrix gt(FieldElement scalar) {

		return comparison(scalar, new GreaterThanComparator());
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are greater
	 * than or equal to those of another Matrices, and zeros elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of ones and zeros
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */
	public Matrix ge(Matrix anotherMatrix) {

		return comparison(anotherMatrix, new GreaterThanOrEqualToComparator(),
				"GE");
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are greater
	 * than or equal to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Matrix of ones and zeros
	 */
	public Matrix ge(FieldElement scalar) {

		return comparison(scalar, new GreaterThanOrEqualToComparator());
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are equal
	 * to those of another Matrices, and zeros elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of ones and zeros
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */
	public Matrix eq(Matrix anotherMatrix) {

		return comparison(anotherMatrix, new EqualToComparator(), "EQ");
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are equal
	 * to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Matrix of ones and zeros
	 */
	public Matrix eq(FieldElement scalar) {

		return comparison(scalar, new EqualToComparator());
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are not
	 * equal to those of another Matrices, and zeros elsewhere.
	 * 
	 * @param anotherMatrix
	 * @return Matrix of ones and zeros
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */
	public Matrix ne(Matrix anotherMatrix) {

		return comparison(anotherMatrix, new NotEqualToComparator(), "NE");
	}

	/**
	 * Returns a Matrix containing ones where this Matrix's elements are not
	 * equal to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Matrix of ones and zeros
	 */
	public Matrix ne(FieldElement scalar) {

		return comparison(scalar, new NotEqualToComparator());
	}

	/**
	 * Sets this Matrix to the result of applying a specified function to every
	 * element of this Matrix. New functions can be applied to a Matrix by
	 * subclassing the abstract <tt>MonadicOperator</tt> class.
	 * 
	 * @param fun
	 *            the function to apply
	 * @return result of applying <tt>fun</tt> to this Matrix
	 */
	public void applyReplace(MonadicOperator fun) {

		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getCols(); j++) {
				this.set(i, j, fun.apply(this.get(i, j)));
			}
		}
	}

	/**
	 * Returns the result of applying a specified function to every element of
	 * this Matrix. New functions can be applied to a Matrix by subclassing the
	 * abstract <tt>MonadicOperator</tt> class.
	 * 
	 * @param fun
	 *            the function to apply
	 * @return result of applying <tt>fun</tt> to this Matrix
	 */
	public Matrix apply(MonadicOperator fun) {

		Matrix matrix = new Matrix(this.getRows(), this.getCols());

		for (int i = 1; i <= matrix.getRows(); i++) {
			for (int j = 1; j <= this.getCols(); j++) {
				matrix.set(i, j, fun.apply(this.get(i, j)));
			}
		}

		return matrix;
	}

	/**
	 * Sets this Matrix to the result of applying a specified function to
	 * elements of this Matrix and another's. New functions can be applied to a
	 * Matrix by subclassing the abstract <tt>DyadicOperator</tt> class.
	 * 
	 * @param anotherMatrix
	 * @param fun
	 *            the function to apply
	 * @return result of applying <tt>fun</tt> to the two Matrices
	 */
	public void applyReplace(Matrix anotherMatrix, DyadicOperator fun) {

		check_sizes(this, anotherMatrix, fun.getClass().getName());

		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getCols(); j++) {
				this.set(i, j, fun.apply(this.get(i, j), anotherMatrix
						.get(i, j)));
			}
		}
	}

	/**
	 * Returns the result of applying a specified function to the elements of
	 * this Matrix and another. New functions can be applied to a Matrix by
	 * subclassing the abstract <tt>DyadicOperator</tt> class.
	 * 
	 * @param anotherMatrix
	 * @param fun
	 *            the function to apply
	 * @return result of applying <tt>fun</tt> to the two Matrices
	 */
	public Matrix apply(Matrix anotherMatrix, DyadicOperator fun) {

		check_sizes(this, anotherMatrix, fun.getClass().getName());

		Matrix matrix = new Matrix(this.getRows(), this.getCols());

		for (int i = 1; i <= matrix.getRows(); i++) {
			for (int j = 1; j <= this.getCols(); j++) {
				matrix.set(i, j, fun.apply(this.get(i, j), anotherMatrix.get(i,
						j)));
			}
		}

		return matrix;
	}

	/**
	 * Sets this Matrix to the result of applying a specified function to
	 * elements of this Matrix and a scalar. New functions can be applied to a
	 * Matrix by subclassing the abstract <tt>DyadicOperator</tt> class.
	 * 
	 * @param scalar
	 * @param fun
	 *            the function to apply
	 * @return result of applying <tt>fun</tt> to this Matrix and the scalar
	 */
	public void applyReplace(FieldElement scalar, DyadicOperator fun) {

		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getCols(); j++) {
				this.set(i, j, fun.apply(this.get(i, j), scalar));
			}
		}
	}

	/**
	 * Returns the result of applying a specified function to the elements of a
	 * this Matrix a scalar. New functions can be applied to a Matrix by
	 * subclassing the abstract <tt>DyadicOperator</tt> class.
	 * 
	 * @param scalar
	 * @param fun
	 *            the function to apply
	 * @return result of applying <tt>fun</tt> to the Matrix and scalar
	 */
	public Matrix apply(FieldElement scalar, DyadicOperator fun) {

		Matrix matrix = new Matrix(this.getRows(), this.getCols());

		for (int i = 1; i <= matrix.getRows(); i++) {
			for (int j = 1; j <= this.getCols(); j++) {
				matrix.set(i, j, fun.apply(this.get(i, j), scalar));
			}
		}

		return matrix;
	}

	/**
	 * Returns the element-wise product of this Matrix and another.
	 * 
	 * @param anotherMatrix
	 * @return this .* anotherMatrix
	 * @throws InvalidOperationException
	 *             if the matrices have different sizes
	 */

	public Matrix arrayMultiply(Matrix anotherMatrix)
			throws InvalidOperationException {

		return operate(this, anotherMatrix, new MultiplyOperator(),
				"arrayMultiply");
	}

	/**
	 * Sets all entries to a FieldElement.
	 * 
	 * @param newEntry
	 *            the FieldElement
	 */

	public void setAll(FieldElement newEntry) {
		for (int i = 1; i <= this.getRows(); ++i) {
			for (int j = 1; j <= this.getCols(); ++j) {
				this.set(i, j, newEntry);
			}
		}
	}

	/**
	 * Computes the sum over all elements of this Matrix.
	 * 
	 * @return the sum
	 */
	public FieldElement sum() {
		return reduce(new SumReduction());
	}

	/**
	 * Computes the mean over all elements of this Matrix.
	 * 
	 * @return the mean
	 */
	public FieldElement mean() {
		return sum().divide(instance(getRows() * getCols()));
	}

	/**
	 * Computes the smallest value of any element in this Matrix.
	 * 
	 * @return the smallest value
	 */
	public FieldElement min() {
		return reduce(new MinReduction());
	}

	/**
	 * Computes the largest value of any element in this Matrix.
	 * 
	 * @return the largest value
	 */
	public FieldElement max() {
		return reduce(new MaxReduction());
	}

	/**
	 * Computes the sum over the rows of this matrix.
	 * 
	 * @return the sum
	 */
	public Vector sumRows() {
		Vector sum = getRow(1);
		for (int i = 2; i <= getRows(); ++i) {
			sum.addReplace(getRow(i));
		}
		return sum;
	}

	/**
	 * Computes the sum over the columns of this matrix.
	 * 
	 * @return the sum
	 */
	public Vector sumCols() {
		return this.transpose().sumRows();
	}

	/**
	 * Computes the mean over the rows of this Matrix.
	 * 
	 * @return the mean
	 */
	public Vector meanRows() {
		return this.sumRows().divide(instance(this.getRows()));
	}

	/**
	 * Computes the mean over the columns of this Matrix.
	 * 
	 * @return the mean
	 */
	public Vector meanCols() {
		return this.sumCols().divide(instance(this.getCols()));
	}

	/**
	 * Returns a new 1xN Vector made from the N elements of this Matrix. Matrix
	 * should have 1 row.
	 * 
	 * @return the Matrix
	 * @throws InvalidOperationException
	 *             if number of rows not equal to one
	 */
	public Vector toVector() throws InvalidOperationException {
		if (getRows() != 1) {
			String err = "Cannot convert multi-row Matrix to Vector";
			throw new InvalidOperationException(err);
		}
		return getRow(1);
	}

	// return Matrix resulting from operation on two others
	private static Matrix operate(Matrix matrix1, Matrix matrix2,
			DyadicOperator fun, String funName) {

		check_sizes(matrix1, matrix2, funName);

		Matrix matrix3 = new Matrix(matrix1.numOfRows, matrix1.numOfCols);
		for (int i = 1; i <= matrix3.getRows(); i++) {
			for (int j = 1; j <= matrix3.getCols(); j++) {
				matrix3.set(i, j, fun.apply(matrix1.get(i, j), matrix2
						.get(i, j)));
			}
		}
		return matrix3;
	}

	// return Matrix resulting from operation on Matrix and scalar
	private static Matrix operate(Matrix matrix, FieldElement scalar,
			DyadicOperator fun) {

		Matrix matrix2 = matrix.copy();

		for (int i = 1; i <= matrix.getRows(); ++i) {
			for (int j = 1; j <= matrix.getCols(); ++j) {
				matrix2.set(i, j, fun.apply(matrix.get(i, j), scalar));
			}
		}

		return matrix2;
	}

	// set elements of this Matrix to result of operation on them and another's
	private void operate(Matrix matrix, DyadicOperator fun, String funName) {

		check_sizes(this, matrix, funName);

		for (int i = 1; i <= this.getRows(); ++i) {
			for (int j = 1; j <= this.getCols(); ++j) {
				this.set(i, j, fun.apply(this.get(i, j), matrix.get(i, j)));
			}
		}
	}

	// set elements of this Matrix to result of operation on them and a scalar
	private void operate(FieldElement scalar, DyadicOperator fun) {

		for (int i = 1; i <= this.getRows(); ++i) {
			for (int j = 1; j <= this.getCols(); ++j) {
				this.set(i, j, fun.apply(this.get(i, j), scalar));
			}
		}
	}

	// return the result of comparing this Matrix with another (ones where
	// comparison succeeds, zeros where it fails)
	private Matrix comparison(Matrix anotherMatrix, FEComparator comp,
			String compName) {

		check_sizes(this, anotherMatrix, compName);

		Matrix a = new Matrix(this.getRows(), this.getCols());

		for (int i = 1; i <= this.getRows(); ++i) {
			for (int j = 1; j <= this.getCols(); ++j) {
				FieldElement entry = this.get(i, j);
				boolean success = comp.compare(entry, anotherMatrix.get(i, j));
				FieldElement result = success ? entry.one() : entry.zero();
				a.set(i, j, result);
			}
		}

		return a;
	}

	// return the result of comparing this Matrix with a scalar (ones where
	// comparison succeeds, zeros where it fails)
	private Matrix comparison(FieldElement scalar, FEComparator comp) {

		Matrix a = new Matrix(this.getRows(), this.getCols());

		for (int i = 1; i <= this.getRows(); ++i) {
			for (int j = 1; j <= this.getCols(); ++j) {
				FieldElement entry = this.get(i, j);
				boolean success = comp.compare(entry, scalar);
				FieldElement result = success ? entry.one() : entry.zero();
				a.set(i, j, result);
			}
		}

		return a;
	}

	// general size checking for two-matrix operations
	private static void check_sizes(Matrix a, Matrix b, String op)
			throws InvalidOperationException {

		if (a.numOfRows != b.getRows() || a.numOfCols != b.getCols()) {
			throw new InvalidOperationException("Tried " + op + "on \n" + a
					+ "\n and \n" + b + "Not correct format!");
		}
	}

	// generic method for sum, min, max
	private FieldElement reduce(Reduction r) {
		r.init(this.get(1, 1));
		for (int i = 1; i <= this.getRows(); i++) {
			for (int j = 1; j <= this.getCols(); j++) {
				if (i != 1 || j != 1) {
					r.track(this.get(i, j));
				}
			}
		}
		return r.reducedValue;
	}

	// return an instance of a value from this matrix
	private FieldElement instance(double n) {
		FieldElement first = this.get(1, 1);
		return first.instance(n);
	}

}
