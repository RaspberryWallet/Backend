package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.io.Serializable;
import java.util.Random;

/**
 * This class provides a set of methods for generating various
 * common types of matrices and vectors for an arbitrary 
 * FieldElement type.
 *
 * @author Simon D. Levy, Andreas Keilhauer
 */

public class LinAlgFactory implements Serializable {

	/**
	 * type of this LinAlgFactory
	 */
	protected FieldElement type = null;

	/**
	 * Creates a LinAlgFactory for a certain FieldElement type.
	 * @param type for this LinAlgFactory
	 */
	public LinAlgFactory(FieldElement type) {
		this.type = type;
	}

	/**
	 * Returns a Matrix of all ones.
	 * @param numberOfRows
	 * @param numberOfCols
	 * @return matrix of ones
	 */
	public Matrix ones(int numberOfRows, int numberOfCols) {
		return block_matrix(numberOfRows, numberOfCols, type.one());
	}

	/**
	 * Returns a Matrix of all zeros.
	 * @param numberOfRows
	 * @param numberOfCols
	 * @return matrix of zeros
	 */
	public Matrix zeros(int numberOfRows, int numberOfCols) {
		return block_matrix(numberOfRows, numberOfCols, type.zero());
	}

	/**
	 * Returns a Matrix of uniformly distributed random values.
	 * The kind of random values you get depends on the
	 * FieldElement you use to initialise the LinAlgFactory
	 * @param numberOfRows
	 * @param numberOfCols
	 * @param random random-number generator
	 * @return matrix of uniformly distributed random values
	 */
	public Matrix uniformNoise(
		int numberOfRows,
		int numberOfCols,
		Random random) {

		Matrix a = new Matrix(numberOfRows, numberOfCols);
		for (int i = 1; i <= numberOfRows; ++i) {
			for (int j = 1; j <= numberOfCols; ++j) {
				a.set(i, j, type.randomValue(random));
			}
		}
		return a;
	}

	/**
	 * Returns a Matrix of normally distributed random values.  Values are
	 * taken from a Gaussian distribution with zero mean and standard 
	 * deviation one.<BR>
	 * <STRONG>Note!</STRONG> Normal Distribution Issue<BR>
	 * For the following FieldElement types this method won't
	 * achieve values of the correct distribution: F2, FieldP
	 * @param numberOfRows
	 * @param numberOfCols
	 * @param random random-number generator
	 * @return Matrix of normally distributed random values
	 */
	public Matrix gaussianNoise(
		int numberOfRows,
		int numberOfCols,
		Random random) {

		Matrix a = new Matrix(numberOfRows, numberOfCols);
		for (int i = 1; i <= numberOfRows; ++i) {
			for (int j = 1; j <= numberOfCols; ++j) {
				a.set(i, j, type.gaussianRandomValue(random));
			}
		}
		return a;
	}

	/**
	 * Returns a Vector of all ones.
	 * @param length vector length
	 * @return vector of ones
	 */
	public Vector ones(int length) {
		return block_vector(length, type.one());
	}

	/**
	 * Returns a Vector of all zeros.
	 * @param length vector length
	 * @return vector of zeros
	 */
	public Vector zeros(int length) {
		return block_vector(length, type.zero());
	}

	/**
	 * Returns a Vector of uniformly distributed random values.
	 * @param length vector length
	 * @return vector of uniformly distributed random values
	 */
	public Vector uniformNoise(int length, Random random) {
		Vector v = new Vector(length);
		for (int i = 1; i <= length; ++i) {
			v.set(i, randomValue(random));
		}
		return v;
	}

	/**
	 * Returns a Vector of uniformly distributed random values.<BR>
	 * <STRONG>Note!</STRONG> Normal Distribution Issue<BR>
	 * For the following FieldElement types this method won't 
	 * achieve values of correct distribution: F2, FieldP
	 * @param length vector length
	 * @return vector of uniformly distributed random values
	 */
	public Vector gaussianNoise(int length, Random random) {
		Vector v = new Vector(length);
		for (int i = 1; i <= length; ++i) {
			v.set(i, gaussianRandomValue(random));
		}
		return v;
	}

	public FieldElement one() {
		return type.one();
	}

	public FieldElement zero() {
		return type.zero();
	}

	/**
	 * Returns a uniformly distributed random value. Refer to the
	 * documentation of the LinAlgFactory's type for further details.
	 * @param random 
	 * @return random value FieldElement
	 */
	public FieldElement randomValue(Random random) {
		return this.type.randomValue(random);
	}

	/**
	 * Returns a random value FieldElement with distribution N(0, 1)
	 * <BR><STRONG>Note!</STRONG> Normal Distribution Issue<BR>
	 * For the following FieldElement  types this method won't 
	 * achieve the correct distribution: F2, FieldP
	 * @param random
	 * @return random value FieldElement
	 */
	public FieldElement gaussianRandomValue(Random random) {
		return this.type.gaussianRandomValue(random);
	}

	/**
	 * Returns the identity Matrix. This is a square matrix with 
	 * ones on its diagonal and zeros elsewhere.
	 * @param size number of rows (= number of columns)
	 * @return identity matrix
	 */
	public Matrix identity(int size) {
		Matrix a = zeros(size, size);
		for (int i = 1; i <= size; ++i) {
			a.set(i, i, type.one());
		}
		return a;
	}

	// return Vector full of value
	private Vector block_vector(int length, FieldElement value) {
		Vector v = new Vector(length);
		for (int i = 1; i <= length; ++i) {
			v.set(i, value);
		}
		return v;
	}

	// return Matrix full of value
	private Matrix block_matrix(
		int numberOfRows,
		int numberOfCols,
		FieldElement value) {
		Matrix a = new Matrix(numberOfRows, numberOfCols);
		for (int i = 1; i <= numberOfRows; ++i) {
			for (int j = 1; j <= numberOfCols; ++j) {
				a.set(i, j, value);
			}
		}
		return a;
	}

	/**
	 * Returns a Matrix built from an array of double-precision floating-point
	 * values.
	 * @param theValues
	 * @return Matrix built from theValues
	 */
	public Matrix buildMatrix(double[][] theValues)
		throws InvalidOperationException {

		return new Matrix(wrap(theValues));
	}

	/**
	 * Returns a Vector built from an array of double-precision floating-point
	 * values.
	 * @param theValues
	 * @return Vector built from theValues
	 */
	public Vector buildVector(double[] theValues)
		throws InvalidOperationException {
		return new Vector(wrap(theValues));
	}

	// I am the Queen / Of the wrapping scene
	public FieldElement[] wrap(double[] x) {
		FieldElement[] d = new FieldElement[x.length];
		for (int i = 0; i < x.length; ++i) {
			d[i] = type.instance(x[i]);
		}
		return d;
	}

	public FieldElement[][] wrap(double[][] x) {
		int m = x.length, numberOfCols, n = x[0].length;
		FieldElement[][] d = new FieldElement[m][n];
		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				d[i][j] = type.instance(x[i][j]);
			}
		}
		return d;
	}

}
