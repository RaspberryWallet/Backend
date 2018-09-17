package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.io.Serializable;

/**
 * This class represents a mathematical vector.
 *
 * @author Andreas Keilhauer, Simon D. Levy
 */

public class Vector implements Serializable{

	protected FieldElement[] entries;

	/**
	 * A constructor that creates a Vector of a given length with no entries.
	 *
	 * @param length
	 */

	public Vector(int length) {
		entries = new FieldElement[length];
	}

	/**
	 * A constructor that puts an Array of FieldElements into a Vector.
	 *
	 * @param theEntries as an array of field elements
	 * @throws InvalidOperationException if the array is null
	 */

	public Vector(FieldElement[] theEntries) throws InvalidOperationException {
		if (theEntries == null) {
			String err = "Tried to construct Vector but entry array was null.";
			throw new InvalidOperationException(err);
		}
		entries = theEntries;
	}

	/**
	 * Returns the length of the Vector.
	 *
	 * @return the length
	 */

	public int length() {
		return entries.length;
	}

	/**
	 * Returns the L1 norm of this Vector.
	 * @return L1 norm of this Vector
	 */
	public FieldElement L1Norm() {
		Vector abs = this.apply(new AbsOperator());
		return abs.sum();
	}

	/**
	 * Returns the Manhattan distance (L1 norm of differences) between
	 * this Vector and another.
	 * @param anotherVector
	 * @return Manhattan distance between this and anotherVector
	 * @throws InvalidOperationException if Vectors have unequal lengths
	 */
	public FieldElement nycDist(Vector anotherVector)
		throws InvalidOperationException {
		return safe_diff(anotherVector, "nycDist").L1Norm();
	}

	/**
	 * Returns the cosine between this Vector and another.  Cosine is the
	 * dot product of the vectors, divided by the product of their lengths.
	 * @param anotherVector
	 * @return cosine between this and anotherVector
	 * @throws InvalidOperationException if Vectors have unequal lengths
	 */
	public FieldElement cosine(Vector anotherVector)
		throws InvalidOperationException {
		check_lengths(this, anotherVector, "cosine");
		double n2 = this.length() * anotherVector.length();
		return this.multiply(anotherVector).divide(wrap(n2));
	}

	/**
	 * Gets the entry of a certain index.  First index is 1.
	 *
	 * @return the entry
	 * @throws InvalidOperationException if the index is out of bounds
	 */

	public FieldElement getEntry(int index) throws InvalidOperationException {
		try {
			return entries[index - 1];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new InvalidOperationException(
				"Invalid index: "
					+ index
					+ "\n Valid only between 1 and "
					+ entries.length);
		}
	}

	/**
	 * Sets an entry to a FieldElement at a certain index.
	 *
	 * @param index
	 * @param newEntry
	 * @throws InvalidOperationException if the index is invalide
	 */

	public void set(int index, FieldElement newEntry)
		throws InvalidOperationException {
		try {
			entries[index - 1] = newEntry;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new InvalidOperationException(
				"Invalid index: "
					+ index
					+ "\n Valid only between 1 and "
					+ entries.length);
		}
	}

	/**
	 * Returns a new 1xN Matrix made from the N elements of this Vector.
	 *
	 * @return the Matrix
	 */
	public Matrix toMatrix() {
		Matrix tmp = new Matrix(this.length(), 1);
		tmp.setCol(1, this);
		return tmp;
	}

	/**
	 * Returns this Vector divided by a scalar.
	 * 
	 * @param scalar
	 * @return the divided Vector
	 */

	public Vector divide(FieldElement scalar) {
		
		return operate(this, scalar, new DivideOperator());
	}

	/**
	 * Divides this Vector by a scalar.
	 * 
	 * @param scalar
	 */

	public void divideReplace(FieldElement scalar) {
		
		operate(scalar, new DivideOperator());
	}

	/**
	 * Returns the product of this Vector and a scalar.
	 * 
	 * @param scalar
	 * @return the multiplied Vector
	 */

	public Vector multiply(FieldElement scalar) {
		
		return operate(this, scalar, new MultiplyOperator());
	}

	/**
	 * Returns the element-wise product of this Vector and another.
	 * 
	 * @param anotherVector
	 * @return this .* anotherVector
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public Vector arrayMultiply(Vector anotherVector)
		throws InvalidOperationException {
		
		return operate(
			this,
			anotherVector,
			new MultiplyOperator(),
			"arrayMultiply");
	}

	/**
	 * Multiplies this Vector by a scalar.
	 * 
	 * @param scalar
	 */

	public void multiplyReplace(FieldElement scalar) {
		
		operate(scalar, new MultiplyOperator());
	}

	/**
	 * Multiplies this Vector element-wise by another.
	 * @param anotherVector
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public void multiplyReplace(Vector anotherVector) {
		
		operate(anotherVector, new MultiplyOperator(), "multiplyReplace");
	}

	/**
	 * Returns the sum of this Vector and a scalar.
	 * 
	 * @param scalar
	 * @return the add Vector
	 */

	public Vector add(FieldElement scalar) {
		
		return operate(this, scalar, new AddOperator());
	}

	/**
	 * Returns the sum of this Vector and another.
	 * 
	 * @param anotherVector
	 * @return this + anotherVector
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public Vector add(Vector anotherVector) throws InvalidOperationException {
		
		return operate(this, anotherVector, new AddOperator(), "add");
	}

	/**
	 * Adds a scalar to this Vector.
	 * 
	 * @param scalar
	 */

	public void addReplace(FieldElement scalar) {
		
		operate(scalar, new AddOperator());
	}

	/**
	 * Adds another vector to this Vector.
	 * @param anotherVector
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public void addReplace(Vector anotherVector) {

		operate(anotherVector, new AddOperator(), "add");
	}

	/**
	 * Returns this Vector subtracted by a scalar.
	 * 
	 * @param scalar
	 * @return the subtractd Vector
	 */

	public Vector subtract(FieldElement scalar) {

		return operate(this, scalar, new SubtractOperator());
	}

	/**
	 * Returns the result of subtracting another vector from this.
	 * 
	 * @param anotherVector
	 * @return this - anotherVector
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public Vector subtract(Vector anotherVector)
		throws InvalidOperationException {

		return operate(this, anotherVector, new SubtractOperator(), "subtract");
	}

	/**
	 * Subtracts a scalar from this Vector.
	 * 
	 * @param scalar
	 */

	public void subtractReplace(FieldElement scalar) {

		operate(scalar, new SubtractOperator());
	}

	/**
	 * Subtracts another Vector from this.
	 * @param anotherVector
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public void subtractReplace(Vector anotherVector) {

		operate(anotherVector, new SubtractOperator(), "subtract");
	}

	/**
	 * Returns the scalar product of this Vector and another.
	 * 
	 * @return The scalar product of this Vector and another one.
	 * @param anotherVector
	 * @throws InvalidOperationException if the other vector is null
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */
	public FieldElement multiply(Vector anotherVector)
		throws InvalidOperationException {

		if (anotherVector == null) {
			throw new InvalidOperationException(
				"Tried to "
					+ "multiply \n"
					+ this
					+ " and \n"
					+ anotherVector
					+ "Second Vector is null!");
		}

		check_lengths(this, anotherVector, "multiply");

		FieldElement result = this.getEntry(1).zero();

		for (int i = 1; i <= entries.length; i++) {
			result =
				result.add(entries[i - 1].multiply(anotherVector.getEntry(i)));
		}

		return result;
	}

	/**
	 * Tests two Vectors for equality.
	 *
	 * @return true if and only if the two Vectors equal in all entries.
	 * @param anotherVector
	 */

	public boolean equals(Vector anotherVector) {
		if (this.length() == anotherVector.length()) {
			for (int i = 1; i <= this.length(); i++) {
				if (!this.getEntry(i).equals(anotherVector.getEntry(i))) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Swaps two Entries of a Vector.
	 *
	 * @param index1 1st swap partner
	 * @param index2 2nd seap partner
	 */

	public void swapEntries(int index1, int index2) {
		FieldElement tmp = this.getEntry(index1);
		this.set(index1, this.getEntry(index2));
		this.set(index2, tmp);
	}

	/**
	 * Returns a String representation of this Vector
	 */

	public String toString() {
		if (entries.length == 0) {
			return "( )";
		}
		String tempString = "( ";
		for (int i = 1; i < entries.length; i++) {
			tempString += getEntry(i) + ", ";
		}
		tempString += getEntry(entries.length) + " )";
		return tempString;
	}

	/**
	 * Returns the logical AND of this Vector with another.  Elements of the
	 * result are 1 where both vectors are non-zero, and zero elsewhere.
	 * @param anotherVector
	 * @return Vector of 1's and 0's
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public Vector and(Vector anotherVector) {

		return operate(this, anotherVector, new AndOperator(), "AND");
	}

	/**
	 * Returns the logical OR of this Vector with another.  Elements of the
	 * result are 1 where both vectors are non-zero, and zero elsewhere.
	 * @param anotherVector
	 * @return Vector of 1's and 0's
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */

	public Vector or(Vector anotherVector) {

		return operate(this, anotherVector, new OrOperator(), "OR");
	}

	/**
	 * Returns the logical negation of this Vector.  Elements of the
	 * result are 1 where the vector is zero, and one elsewhere.
	 * @return Vector of 1's and 0's
	 */

	public Vector not() {

		return this.apply(new NotOperator());
	}

	/**
	 * Sets this Vector to the result of applying a specified function
	 * to every element of this Vector. New functions can be applied
	 * to a Vector by subclassing the abstract <tt>MonadicOperator</tt>
	 * class.
	 * @param fun the function to apply
	 * @return result of applying <tt>fun</tt> to this Vector
	 */
	public void applyReplace(MonadicOperator fun) {

		for (int i = 1; i <= this.length(); i++) {
			this.set(i, fun.apply(this.getEntry(i)));
		}
	}

	/**
	 * Returns the result of applying a specified function to every
	 * element of this Vector. New functions can be applied to a Vector
	 * by subclassing the abstract <tt>MonadicOperator</tt> class.
	 * @param fun the function to apply
	 * @return result of applying <tt>fun</tt> to this Vector
	 */
	public Vector apply(MonadicOperator fun) {

		Vector vector = new Vector(this.length());

		for (int i = 1; i <= vector.length(); i++) {
			vector.set(i, fun.apply(this.getEntry(i)));
		}

		return vector;
	}

	/**
	 * Sets this Vector to the result of applying a specified function
	 * to elements of this Vector and another's. New functions can be applied
	 * to a Vector by subclassing the abstract <tt>DyadicOperator</tt>
	 * class.
	 * @param anotherVector
	 * @param fun the function to apply
	 * @return result of applying <tt>fun</tt> to the two Vectors
	 */
	public void applyReplace(Vector anotherVector, DyadicOperator fun) {

		check_lengths(this, anotherVector, fun.getClass().getName());

		for (int i = 1; i <= this.length(); i++) {
			this.set(
				i,
				fun.apply(this.getEntry(i), anotherVector.getEntry(i)));
		}
	}

	/**
	 * Returns the result of applying a specified function to the
	 * elements of this Vector and another. New functions can be
	 * applied to a Vector by subclassing the abstract
	 * <tt>DyadicOperator</tt> class.
	 * @param anotherVector
	 * @param fun the function to apply
	 * @return result of applying <tt>fun</tt> to the two Vectors
	 */
	public Vector apply(Vector anotherVector, DyadicOperator fun) {

		check_lengths(this, anotherVector, fun.getClass().getName());

		Vector vector = new Vector(this.length());

		for (int i = 1; i <= vector.length(); i++) {
			vector.set(
				i,
				fun.apply(this.getEntry(i), anotherVector.getEntry(i)));
		}

		return vector;
	}

	/**
	 * Sets this Vector to the result of applying a specified function
	 * to elements of this Vector and a scalar. New functions can be applied
	 * to a Vector by subclassing the abstract <tt>DyadicOperator</tt>
	 * class.
	 * @param scalar
	 * @param fun the function to apply
	 * @return result of applying <tt>fun</tt> to this Vector and the scalar
	 */
	public void applyReplace(FieldElement scalar, DyadicOperator fun) {

		for (int i = 1; i <= this.length(); i++) {
			this.set(i, fun.apply(this.getEntry(i), scalar));
		}
	}

	/**
	 * Returns the result of applying a specified function to the
	 * elements of a this Vector a scalar. New functions can be
	 * applied to a Vector by subclassing the abstract
	 * <tt>DyadicOperator</tt> class.
	 * @param scalar
	 * @param fun the function to apply
	 * @return result of applying <tt>fun</tt> to the Vector and scalar
	 */
	public Vector apply(FieldElement scalar, DyadicOperator fun) {

		Vector vector = new Vector(this.length());

		for (int i = 1; i <= vector.length(); i++) {
			vector.set(i, fun.apply(this.getEntry(i), scalar));
		}

		return vector;
	}

	/**
	 * Returns a deep copy of this Vector.
	 *
	 * @return Vector copy
	 */

	public Vector copy() {
		Vector tmp = new Vector(this.length());
		for (int i = 1; i <= this.length(); i++) {
			tmp.set(i, this.getEntry(i));
		}
		return tmp;
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements are less
	 * than those of another Vectors, and zeros elsewhere.
	 * 
	 * @param anotherVector
	 * @return Vector of ones and zeros
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */
	public Vector lt(Vector anotherVector) {

		return comparison(anotherVector, new LessThanComparator(), "LT");
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements are less
	 * than a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Vector of ones and zeros
	 */
	public Vector lt(FieldElement scalar) {

		return comparison(scalar, new LessThanComparator());
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements are less
	 * than or equal to those of another Vectors, and zeros elsewhere.
	 * 
	 * @param anotherVector
	 * @return Vector of ones and zeros
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */
	public Vector le(Vector anotherVector) {

		return comparison(
			anotherVector,
			new LessThanOrEqualToComparator(),
			"LE");
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements are less
	 * than or equal to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Vector of ones and zeros
	 */
	public Vector le(FieldElement scalar) {

		return comparison(scalar, new LessThanOrEqualToComparator());
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements
	 * are greater than those of another Vectors, and zeros elsewhere.
	 * 
	 * @param anotherVector
	 * @return Vector of ones and zeros
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */
	public Vector gt(Vector anotherVector) {

		return comparison(anotherVector, new GreaterThanComparator(), "GT");
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements are
	 * greater than a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Vector of ones and zeros
	 */
	public Vector gt(FieldElement scalar) {

		return comparison(scalar, new GreaterThanComparator());
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements
	 * are greater than or equal to those of another Vectors, and
	 * zeros elsewhere.
	 * 
	 * @param anotherVector
	 * @return Vector of ones and zeros
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */
	public Vector ge(Vector anotherVector) {

		return comparison(
			anotherVector,
			new GreaterThanOrEqualToComparator(),
			"GE");
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements are
	 * greater than or equal to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Vector of ones and zeros
	 */
	public Vector ge(FieldElement scalar) {

		return comparison(scalar, new GreaterThanOrEqualToComparator());
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements
	 * are equal to those of another Vectors, and zeros elsewhere.
	 * 
	 * @param anotherVector
	 * @return Vector of ones and zeros
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */
	public Vector eq(Vector anotherVector) {

		return comparison(anotherVector, new EqualToComparator(), "EQ");
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements
	 * are equal to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Vector of ones and zeros
	 */
	public Vector eq(FieldElement scalar) {

		return comparison(scalar, new EqualToComparator());
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements
	 * are not equal to those of another Vector, and zeros elsewhere.
	 * 
	 * @param anotherVector
	 * @return Vector of ones and zeros
	 * @throws InvalidOperationException if the vectors have unequal lengths
	 */
	public Vector ne(Vector anotherVector) {

		return comparison(anotherVector, new NotEqualToComparator(), "NE");
	}

	/**
	 * Returns a Vector containing ones where this Vector's elements
	 * are not equal to a scalar, and zeros elsewhere.
	 * 
	 * @param scalar
	 * @return Vector of ones and zeros
	 */
	public Vector ne(FieldElement scalar) {

		return comparison(scalar, new NotEqualToComparator());
	}

	/**
	 * Computes the sum over the elements of this Vector.
	 * @return the sum
	 */
	public FieldElement sum() {
		return reduce(new SumReduction());
	}

	/**
	 * Computes the smallest value of any element in this Vector.
	 * @return the smallest value
	 */
	public FieldElement min() {
		return reduce(new MinReduction());
	}

	/**
	 * Computes the largest value of any element in this Vector.
	 * @return the largest value
	 */
	public FieldElement max() {
		return reduce(new MaxReduction());
	}

	/**
	 * Computes the mean over the elements of this Vector.
	 * @return the mean
	 */
	public FieldElement mean() {
		return this.sum().divide(wrap(this.length()));
	}

	/**
	 * Returns indices where vector equals scalar argument.  
	 * @param scalar 
	 * @return indices where vector equals scalar
	 */
	public int[] find(FieldElement scalar) {
		int[] equals = new int[this.length()];
		int n = 0;
		for (int i = 1; i <= this.length(); ++i) {
			FieldElement val = this.getEntry(i);
			equals[i - 1] = val.equals(scalar) ? i : -1;
			if (val.equals(scalar))
				n++;
		}
		int[] indices = new int[n];
		int j = 0;
		for (int i = 0; i < this.length(); ++i) {
			if (equals[i] > -1) {
				indices[j++] = equals[i];
			}
		}
		return indices;
	}

	/**
	 * Sets all entries to a FieldElement.
	 *
	 * @param newEntry the FieldElement
	 */

	public void setAll(FieldElement newEntry) {
		for (int i = 1; i <= this.length(); ++i) {
			this.set(i, newEntry);
		}
	}

	/**
	 * Replicates this Vector as a Matrix.  Returns an
	 * <i>m</i>-by-<i>n</i> Matrix for a length-<i>n</i> Vector replicated
	 * <i>m</i> times.
	 *
	 * @param m number of times to replicate
	 * @return an <i>m</i>-by-<i>n</i> Matrix
	 */
	public Matrix repmat(int m) {
		Matrix a = new Matrix(m, this.length());
		for (int i = 1; i <= m; ++i) {
			a.setRow(i, this);
		}
		return a;
	}

	/**
	 * Returns a copy of this Vector, sorted in ascending order.
	 * @return sorted copy
	 */
	public Vector sort() {
		Vector sorted = this.copy();
		java.util.Arrays.sort(sorted.entries);
		return sorted;
	}

	/**
	 * Returns the product of this Vector and a Matrix.
	 * 
	 * @param theMatrix
	 * @return this * theMatrix
	 * @throws InvalidOperationException if the matrix is null
	 * @throws InvalidOperationException if the inner dimensions mismatch
	 */
	public Vector multiply(Matrix theMatrix) throws InvalidOperationException {

		if (theMatrix == null) {
			throw new InvalidOperationException(
				"Tried to "
					+ "multiply \n"
					+ this
					+ " and \n"
					+ theMatrix
					+ "Matrix is null!");
		}

		if (this.length() != theMatrix.getRows()) {
			throw new InvalidOperationException(
				"Tried to "
					+ "multiply \n"
					+ this
					+ " and \n"
					+ theMatrix
					+ "Inner dimensions do not match!");
		}

		Vector result = new Vector(theMatrix.getCols());

		for (int i = 1; i <= theMatrix.getCols(); ++i) {
			result.set(i, this.multiply(theMatrix.getCol(i)));
		}

		return result;
	}

	/**
	 * Returns the cross-product of this Vector and another.
	 * 
	 * @param  anotherVector
	 * @return this^T X anotherVector
	 * @throws InvalidOperationException if the other Vector is null
	 */
	public Matrix cross(Vector anotherVector)
		throws InvalidOperationException {

		if (anotherVector == null) {
			throw new InvalidOperationException(
				"Tried to "
					+ "cross \n"
					+ this
					+ " and \n"
					+ anotherVector
					+ "Other vector is null!");
		}

		Matrix result = new Matrix(this.length(), anotherVector.length());

		for (int i = 1; i <= this.length(); ++i) {
			for (int j = 1; j <= anotherVector.length(); ++j) {
				result.set(
					i,
					j,
					this.getEntry(i).multiply(anotherVector.getEntry(j)));
			}
		}

		return result;
	}

	/**
	 * Tests whether this is the zero-vector.
	 * 
	 * @return true iff this is the zero vector
	 */
	
	public boolean isZero(){
		for(int i=0; i<this.length(); i++){
			if(!this.entries[i].isZero()){
				return false;
			}
		}
		return true;
	}
	
	// subtraction with error check
	protected Vector safe_diff(Vector anotherVector, String method)
		throws InvalidOperationException {
		check_lengths(this, anotherVector, method);
		return this.subtract(anotherVector);
	}

	// return the result of comparing this Vector with another (ones where
	// comparison succeeds, zeros where it fails)
	private Vector comparison(
		Vector anotherVector,
		FEComparator comp,
		String compName) {

		check_lengths(this, anotherVector, compName);

		Vector v = new Vector(this.length());

		for (int i = 1; i <= this.length(); ++i) {
			FieldElement entry = this.getEntry(i);
			boolean success = comp.compare(entry, anotherVector.getEntry(i));
			FieldElement result = success ? entry.one() : entry.zero();
			v.set(i, result);
		}

		return v;
	}

	// return the result of comparing this Vector with a scalar (ones where
	// comparison succeeds, zeros where it fails)
	private Vector comparison(FieldElement scalar, FEComparator comp) {

		Vector v = new Vector(this.length());

		for (int i = 1; i <= this.length(); ++i) {
			FieldElement entry = this.getEntry(i);
			boolean success = comp.compare(entry, scalar);
			FieldElement result = success ? entry.one() : entry.zero();
			v.set(i, result);
		}

		return v;
	}

	// makes an appropriate FieldElement for this Vector
	private FieldElement wrap(double n) {
		FieldElement first = this.getEntry(1);
		return first.instance(n);
	}

	// return Vector resulting from operation on two others
	private static Vector operate(
		Vector vector1,
		Vector vector2,
		DyadicOperator fun,
		String funName) {

		check_lengths(vector1, vector2, funName);

		Vector vector3 = vector1.copy();

		for (int i = 1; i <= vector1.length(); ++i) {
			vector3.set(
				i,
				fun.apply(vector1.getEntry(i), vector2.getEntry(i)));
		}

		return vector3;
	}

	// return Vector resulting from operation on Vector and scalar
	private static Vector operate(
		Vector vector,
		FieldElement scalar,
		DyadicOperator fun) {

		Vector vector2 = vector.copy();

		for (int i = 1; i <= vector.length(); ++i) {
			vector2.set(i, fun.apply(vector.getEntry(i), scalar));
		}

		return vector2;
	}

	// set elements of this Vector to result of operation on them and another's
	private void operate(Vector vector, DyadicOperator fun, String funName) {

		check_lengths(this, vector, funName);

		for (int i = 1; i <= this.length(); ++i) {
			this.set(i, fun.apply(this.getEntry(i), vector.getEntry(i)));
		}
	}

	// set elements of this Vector to result of operation on them and a scalar
	private void operate(FieldElement scalar, DyadicOperator fun) {

		for (int i = 1; i <= this.length(); ++i) {
			this.set(i, fun.apply(this.getEntry(i), scalar));
		}
	}

	// generic method for sum, min, max
	private FieldElement reduce(Reduction r) {
		r.init(this.getEntry(1));
		for (int i = 2; i <= this.length(); i++) {
			r.track(this.getEntry(i));
		}
		return r.reducedValue;
	}

	// check vectors for equal length, throwing exception on failure
	private static void check_lengths(Vector x, Vector y, String op)
		throws InvalidOperationException {
		if (x.length() != y.length()) {
			String err =
				"Tried to calculate\n"
					+ x
					+ " "
					+ op
					+ "\n"
					+ y
					+ " of different lengths";
			throw new InvalidOperationException(err);
		}
	}

}
