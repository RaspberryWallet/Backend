package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

/**
 * This class represents a DiagonalMatrix.
 *
 * @author Lei Chen
 * @author Veronika Ortner 
 * @author Safak Oekmen
 */

class DiagonalMatrix extends Matrix {

	/*
	* CONSTRUCTORS:
	* Constructor1:
	* DiagonalMatrix arising from array containing diagonal Elements.
	*/

	/**
	 * @param diagElement array containing diagonal Elements.
	 */

	public DiagonalMatrix(FieldElement[] diagElements)
		throws InvalidOperationException {

		super(diagElements.length, diagElements.length);

		int arraysize = diagElements.length;

		if (diagElements == null)
			throw new InvalidOperationException("Tried to construct DiagonalMatrix but diagElements array was null.");

		for (int i = 0; i < arraysize; i++)
			for (int j = 0; j < arraysize; j++) {
				if (i != j)
					//zero Element of FieldElement
					entries[i][j] = diagElements[0].zero();
				else
					entries[i][j] = diagElements[i];
			}
	}

	/*
	 * Constructor2:
	 * DiagonalMatrix in given size. 
	 * Diagonal Elements are set diagElement.
	 */

	/**
	 * @param size size of DiagonalMatrix to be constructed
	     * @param diagElement value of diagonal Elements
	 */

	public DiagonalMatrix(int size, FieldElement diagElement) {

		super(size, size);

		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				if (i == j)
					entries[i][j] = diagElement;
				else
					entries[i][j] = diagElement.zero();
			}
	}

	/*
	 * Methods:
	 * 
	 * ADOPTED METHODS:
	 * getRows,getCols,getRow,getCol,withoutRow,
	     * withoutCol,insertRow,insertCol,toString,detCalc,
	     * isZeroRow,isZeroCol,hermitian
	 * 
	 * TRANSCRIBED METHODS:
	 * getEntry,setEntry,setRow,setCol,add,
	     * subtract,multiply,copy,det,swapRows,
	     * swapCols,rank,inverse,transpose,equals,
	 * gausselim,gaussjord
	 * 
	 * NEW METHODS:
	 * getDiagElement,setDiagElement,setRC,contZeroRow,getDiagonalElements
	 * toMatrix
	 */

	/* 
	 * getEntry:
	 * Getting particular Element at a given position in a DiagonalMatrix
	 */

	/**
	 * @param rowIndex index of row in DiagonalMatrix
	 * @param colIndex index of col in DiagonalMatrix
	 * @return FieldElement
	 */

	public FieldElement get(int rowIndex, int colIndex) {

		if (rowIndex != colIndex)
			return this.get(1, 1).zero();
		else
			return getDiagElement(rowIndex);
	}

	/* 
	* setEntry:
	* Setting particular Element at given position in DiagonalMatrix
	*/

	/**
	 * @param rowIndex index of row in DiagonalMatrix
	 * @param colIndex index of col in DiagonalMatrix
	 * @param newEntry FieldElement to be set
	 */

	public void set(int rowIndex, int colIndex, FieldElement newEntry)
		throws InvalidOperationException {

		if (rowIndex != colIndex)
			throw new InvalidOperationException("Tried to set non-diagonal entry to a value different from zero.");
		else
			setDiagElement(rowIndex, newEntry);
	}

	/**
	 * @param position row or column Index of FieldElement 
	 * @return FieldElement
	 */

	public FieldElement getDiagElement(int position)
		throws InvalidOperationException {

		int size = this.numOfRows;

		if ((position > size) || (position < 0))
			throw new InvalidOperationException(
				"Tried to get Element at Position "
					+ position
					+ ". Size of Matrix:\n "
					+ size);
		else
			return this.entries[position - 1][position - 1];
	}

	/**
	 * @param position row or column index of DiagonalMatrix 
	 * @param elem FieldElement to be set
	 */

	public void setDiagElement(int position, FieldElement elem)
		throws InvalidOperationException {

		int size = this.numOfRows;

		if ((position > size) || (position < 0))
			throw new InvalidOperationException(
				"Tried to set Element at Position\n"
					+ position
					+ ". Size of Matrix:\n "
					+ size);
		else
			this.entries[position - 1][position - 1] = elem;
	}

	/*
	 * setRow/setCol
	 * replacing row or column with given vector
	 */

	/**
	 * @param rowIndex index of row to be replaced
	 * @param vector vector to be set
	 */

	public void setRow(int rowIndex, Vector vector) {

		setRC(rowIndex, vector);

	}

	/**
	* @param colIndex index of col to be replaced
	* @param vector vector to be set
	*/

	public void setCol(int colIndex, Vector vector) {

		setRC(colIndex, vector);

	}

	/**
	 * @param rcIndex row or column index of DiagonalMatrix
	 * @param vector vector to be set
	 */

	private void setRC(int rcIndex, Vector vector)
		throws InvalidOperationException {

		int vectorsize = vector.length();

		int size = this.numOfRows;

		if (size != vectorsize)
			throw new InvalidOperationException("Tried to set a row with a voctor of invalid size.");

		for (int i = 1; i <= vectorsize; i++) {
			if (i != rcIndex) {
				if (!(vector.getEntry(i).equals(vector.getEntry(1).zero())))
					throw new InvalidOperationException("Tried to set a non-diagonal entry to a value different from zero.");
			}
		}
		setDiagElement(rcIndex, vector.getEntry(rcIndex));
	}

	/*
	 * add/subtract
	 */

	/**
	 * @param diagMatrix diagonal Matrix to be added 
	 * @return sum of diagonal matrices
	 */

	public DiagonalMatrix add(DiagonalMatrix diagMatrix)
		throws InvalidOperationException {

		if (this.numOfCols != diagMatrix.numOfRows)
			throw new InvalidOperationException(
				"Tried to sum up \n"
					+ this
					+ "and \n"
					+ diagMatrix
					+ "No correct format!");

		DiagonalMatrix tmp = (DiagonalMatrix) this.copy();

		for (int i = 1; i <= tmp.getRows(); i++)
			tmp.set(
				i,
				i,
				this.get(i, i).add(diagMatrix.get(i, i)));

		return tmp;
	}

	/*
	 * adding square matrix not being DiagonalMatrix
	 */

	/**
	 * @param matrix matrix to be added to this DiagonalMatrix
	 * @return addition of this DiagonalMatrix plus matrix
	 */

	public Matrix add(Matrix matrix) throws InvalidOperationException {

		//size of this: axa; size of matrix: cxd or axd
		if ((this.numOfCols != matrix.numOfRows)
			|| (this.numOfCols != matrix.numOfCols))
			throw new InvalidOperationException(
				"Tried to sum up \n"
					+ this
					+ "and \n"
					+ matrix
					+ "No correct format!");

		Matrix tmp = matrix.copy();

		for (int i = 1; i <= tmp.numOfRows; i++)
			tmp.set(i, i, this.get(i, i).add(matrix.get(i, i)));

		return tmp;
	}

	/**
	 * @param diagMatrix matrix to be subtracted from this DiagonalMatrix
	 * @return Difference of this DiagonalMatrix and diagMatrix 
	 */

	public DiagonalMatrix subtract(DiagonalMatrix diagMatrix)
		throws InvalidOperationException {

		//size of each DiagonalMatrix is different
		if (this.numOfCols != diagMatrix.numOfRows)
			throw new InvalidOperationException(
				"Tried to subtract \n"
					+ diagMatrix
					+ "from \n"
					+ this
					+ "No correct format!");

		DiagonalMatrix tmp =
			new DiagonalMatrix(
				this.numOfRows,
				diagMatrix.get(1, 1).zero());

		for (int i = 1; i <= tmp.numOfRows; i++)
			tmp.set(
				i,
				i,
				this.get(i, i).subtract(diagMatrix.get(i, i)));
		return tmp;

	}

	/**
	 * @param matrix matrix to be subtracted from this DiagonalMatrix
	 * @return Difference between this DiagonalMatrix and Matrix 
	 */

	public Matrix subtract(Matrix matrix) throws InvalidOperationException {

		if (this.numOfCols != matrix.numOfRows)
			throw new InvalidOperationException(
				"Tried to subtract \n"
					+ matrix
					+ "from \n"
					+ this
					+ "No correct format!");

		Matrix tmp = new Matrix(this.numOfRows, matrix.numOfCols);

		for (int i = 1; i <= tmp.numOfRows; i++)
			for (int j = 1; j <= tmp.numOfCols; j++)
				tmp.set(
					i,
					j,
					this.get(i, j).subtract(matrix.get(i, j)));

		return tmp;
	}

	/*
	     * Returns a DiagonalMatrix that is this DiagonalMatrix multiplied with a scalar
	 */

	/**
	 * @param scalar scalar FieldElement that is multiplied to this DiagonalMatrix
	 * scalar * DiagonalMatrix
	 * @return DiagonalMatrix
	 */

	public Matrix multiply(FieldElement scalar) {

		int size = this.getRows();

		DiagonalMatrix diagMatrix = new DiagonalMatrix(size, scalar.zero());

		for (int i = 0; i < size; i++)
			diagMatrix.entries[i][i] = this.entries[i][i].multiply(scalar);

		return diagMatrix;
	}

	/*
	 * Returns vector that is the product of this 
	 * DiagonalMatrix and given vector. Multiplication from right.
	 */

	/**
	 * @param vector vector that is multiplied to this DiagonalMatrix
	 * this * vector
	 * @return Vector
	 */

	public Vector multiply(Vector vector) throws InvalidOperationException {

		if (this.numOfCols != vector.length()) {
			throw new InvalidOperationException(
				"Tried to multiply \n"
					+ this
					+ "and \n"
					+ vector
					+ "No correct format!");

		}

		FieldElement[] result = new FieldElement[this.numOfRows];

		for (int i = 1; i <= this.getRows(); i++)
			result[i - 1] = entries[i - 1][i - 1].multiply(vector.getEntry(i));

		Vector resultVector = new Vector(result);

		return resultVector;
	}

	/*
	 * Matrix that is the product of this DiagonalMatrix 
	 * and another Matrix.
	 * Multiplication from right
	 */

	/**
	 * @param matrix matrix that is multiplied to this DiagonalMatrix
	 * "this * matrix"
	 * @return Matrix
	 */

	public Matrix multiply(Matrix matrix) throws InvalidOperationException {

		if (this.numOfCols != matrix.getRows()) {
			throw new InvalidOperationException(
				"Tried to multiply \n"
					+ this
					+ "and \n"
					+ matrix
					+ "No correct format!");

		}

		Matrix resultMatrix = new Matrix(this.getRows(), matrix.getCols());

		for (int i = 1; i <= matrix.numOfRows; i++)
			for (int j = 1; j <= matrix.numOfCols; j++)
				resultMatrix.set(
					i,
					j,
					this.entries[i - 1][i - 1].multiply(matrix.get(i, j)));

		return resultMatrix;

	}

	/*
	* Matrix that is the product of this DiagonalMatrix 
	* and another DiagonalMatrix.
	* Multiplication from right
	*/

	/**
	* @param diagMatrix DiagonalMatrix that is multiplied to this DiagonalMatrix
	* "this * diagMatrix"
	* @return DiagonalMatrix
	*/

	public DiagonalMatrix multiply(DiagonalMatrix diagMatrix)
		throws InvalidOperationException {

		if (this.numOfCols != diagMatrix.getRows())
			throw new InvalidOperationException(
				"Tried to multiply \n"
					+ this
					+ "and \n"
					+ diagMatrix
					+ "No correct format!");

		DiagonalMatrix result =
			new DiagonalMatrix(
				this.getRows(),
				diagMatrix.get(1, 1).zero());

		for (int i = 1; i <= diagMatrix.numOfRows; i++)
			result.setDiagElement(
				i,
				this.entries[i - 1][i - 1].multiply(diagMatrix.get(i, i)));

		return result;
	}

	/*
	 * copy()
	 */

	/**
	 * @return DiagonalMatrix
	 */

	public Matrix copy() {

		DiagonalMatrix result =
			new DiagonalMatrix(this.getRows(), this.get(1, 1).zero());

		for (int i = 0; i < getRows(); i++)
			result.entries[i][i] = this.entries[i][i];

		return result;
	}

	/*
	 * det()
	 */

	/**
	 * @param dm DiagonalMatrix for which the determinant is calculated
	 * @return FieldElement
	 */

	public FieldElement det() {

		FieldElement result = this.get(1, 1).one();

		FieldElement[] diagonalElements = getDiagonalElements();

		for (int i = 0; i < getRows(); i++)
			result = result.multiply(diagonalElements[i]);

		return result;
	}

	/*
	 * swapRows,swapCols
	 */

	/**
	 * @param rowIndex1 index of first swap partner.
	 * @param rowIndex1 index of second swap partner.
	 */

	public void swapRows(int rowIndex1, int rowIndex2)
		throws InvalidOperationException {

		throw new InvalidOperationException("swapRows is not allowed in diagonal matrices.");

	}

	/**
	 * @param colIndex1 index of first swap partner.
	 * @param colIndex1 index of second swap partner.
	 */

	public void swapCols(int colIndex1, int colIndex2)
		throws InvalidOperationException {

		throw new InvalidOperationException("swapCols is not allowed in diagonal matrices.");
	}

	/*
	 * rank() 
	 */

	public int rank() {

		int nonZeroRows = 0;

		for (int i = 0; i < this.numOfRows; i++) {
			if (!(isZeroRow(i + 1)))
				nonZeroRows++;
		}

		return nonZeroRows;
	}

	/*
	 * contZeroRow()
	 */

	/**
	 * @return Boolean
	 */

	public boolean contZeroRow() {

		for (int i = 1; i <= numOfRows; i++) {
			if (isZeroRow(i))
				return true;
		}
		return false;

	}

	/*
	 * getDiagonalElements()
	 */

	/**
	 * @return FieldElement[]
	 */

	public FieldElement[] getDiagonalElements() {

		FieldElement[] diagElements = new FieldElement[numOfRows];

		for (int i = 0; i < numOfRows; i++)
			diagElements[i] = entries[i][i];

		return diagElements;

	}

	/*
	 * inverse
	 */

	/**
	 * @return DiagonalMatrix
	 */

	public Matrix inverse() throws InvalidOperationException {

		DiagonalMatrix reverse =
			new DiagonalMatrix(this.numOfRows, this.get(1, 1).zero());

		if (this.contZeroRow())
			throw new InvalidOperationException("Not invertible.");

		else {

			for (int i = 1; i <= numOfRows; i++)
				reverse.set(i, i, this.get(i, i).invert());

		}

		return reverse;

	}

	/*
	 * transpose()
	 */

	/**
	 * @return DiagonalMatrix
	 */

	public Matrix transpose() {

		return this.copy();
	}

	/*
	 * equals
	 */

	/**
	 * @param anotherMatrix Matrix that is collated to this DiagonalMatrix
	 * @return Boolean
	 */

	public boolean equals(DiagonalMatrix anotherDiagMatrix) {

		if (anotherDiagMatrix.getRows() != this.getRows())
			return false;

		else {

			for (int i = 0; i < numOfRows; i++) {

				if (entries[i][i] != anotherDiagMatrix.entries[i][i])
					return false;

			}
			return true;
		}

	}

	/*
	 * gausselim & gaussjord
	 */

	/**
	 * @return DiagonalMatrix
	 */

	public Matrix gaussjord() {

		return this.copy();

	}

	/**
	 * @return DiagonalMatrix
	 */

	public Matrix gausselim() {

		return this.copy();
	}

	/**
	 * @return Matrix
	 */

	public Matrix toMatrix() {

		Matrix result = new Matrix(this.numOfRows, this.numOfCols);

		for (int i = 1; i <= this.numOfRows; i++)
			for (int j = 1; j <= this.numOfCols; j++)
				result.set(i, j, this.get(i, j));

		return result;

	}

}
