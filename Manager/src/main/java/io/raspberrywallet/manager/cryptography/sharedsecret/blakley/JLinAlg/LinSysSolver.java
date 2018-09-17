package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Stack;

/**
 * This class is capable of solving linear equations.
 * 
 * @author Andreas Keilhauer
 */

public class LinSysSolver implements Serializable {

	/**
	 * Calculates the solution space of a given linear equation system of the
	 * form A*x=b.
	 * 
	 * @param a
	 *            coefficient matrix
	 * @param b
	 *            result vector
	 * @return solution space as an AffinLinearSubSpace.
	 */

	public static AffineLinearSubspace solutionSpace(Matrix a, Vector b)
			throws InvalidOperationException {

		if (a.getRows() != b.length()) {
			throw new InvalidOperationException(
					"Tried to solve an equation system with a coefficient matrix"
							+ " with " + a.getRows() + " rows and a"
							+ " vector with length " + b.length()
							+ ". Not correct format!");
		}

		Matrix extCoeff = LinSysSolver.isSolvableHelper(a, b);

		if (extCoeff == null) {
			return null;
		}

		Stack swaps = new Stack();
		for (int row = 1; row <= extCoeff.getRows(); row++) {
			if (extCoeff.get(row, row).isZero()) {
				int col = row;
				while (extCoeff.get(row, col).isZero()) {
					col++;
				}
				swaps.push(new Integer(row));
				swaps.push(new Integer(col));
				extCoeff.swapCols(row, col);
			}
		}

		int dimension = a.getCols();

		FieldElement zero = extCoeff.get(1, 1).zero();
		FieldElement minusOne = zero.subtract(zero.one());

		Vector[] generatingSystem = new Vector[extCoeff.getCols()
				- extCoeff.getRows() - 1];

		for (int col = extCoeff.getRows() + 1; col <= extCoeff.getCols() - 1; col++) {
			Vector tmp = new Vector(dimension);
			for (int row = 1; row <= dimension; row++) {
				if (row <= extCoeff.getRows()) {
					tmp.set(row, extCoeff.get(row, col));
				} else if (row == col) {
					tmp.set(row, minusOne);
				} else {
					tmp.set(row, zero);
				}
			}

			generatingSystem[col - (extCoeff.getRows() + 1)] = tmp;
		}

		Vector inhomogenousPart = new Vector(dimension);

		for (int row = 1; row <= dimension; row++) {
			if (row <= extCoeff.getRows()) {
				inhomogenousPart
						.set(row, extCoeff.get(row, extCoeff.getCols()));
			} else {
				inhomogenousPart.set(row, zero);
			}
		}

		while (!swaps.isEmpty()) {
			int index1 = ((Integer) swaps.pop()).intValue();
			int index2 = ((Integer) swaps.pop()).intValue();
			inhomogenousPart.swapEntries(index1, index2);
			for (int i = 0; i < generatingSystem.length; i++) {
				generatingSystem[i].swapEntries(index1, index2);
			}
		}

		if (inhomogenousPart.isZero()) {
			return new LinearSubspace(generatingSystem);
		} else {
			return new AffineLinearSubspace(inhomogenousPart, generatingSystem,
					true);
		}

	}

	/**
	 * Calculates a solution of a given linear equation system of the form
	 * A*x=b.
	 * 
	 * @param a
	 *            coefficient matrix
	 * @param b
	 *            result vector
	 * @return solution as a Vector.
	 */

	public static Vector solve(Matrix a, Vector b) {

		if (a.getRows() != b.length()) {
			throw new InvalidOperationException(
					"Tried to solve an equation system with a coefficient matrix"
							+ " with " + a.getRows() + " rows and a"
							+ " vector with length " + b.length()
							+ ". Not correct format!");
		}

		Matrix extCoeff = LinSysSolver.isSolvableHelper(a, b);

		if (extCoeff == null) {
			return null;
		}

		Stack swaps = new Stack();
		for (int row = 1; row <= extCoeff.getRows(); row++) {
			if (extCoeff.get(row, row).isZero()) {
				int col = row;
				while (extCoeff.get(row, col).isZero()) {
					col++;
				}
				swaps.push(new Integer(row));
				swaps.push(new Integer(col));
				extCoeff.swapCols(row, col);
			}
		}

		int dimension = a.getCols();

		FieldElement zero = extCoeff.get(1, 1).zero();
		Vector inhomogenousPart = new Vector(dimension);

		for (int row = 1; row <= dimension; row++) {
			if (row <= extCoeff.getRows()) {
				inhomogenousPart
						.set(row, extCoeff.get(row, extCoeff.getCols()));
			} else {
				inhomogenousPart.set(row, zero);
			}
		}

		while (!swaps.isEmpty()) {
			int index1 = ((Integer) swaps.pop()).intValue();
			int index2 = ((Integer) swaps.pop()).intValue();
			inhomogenousPart.swapEntries(index1, index2);
		}

		return inhomogenousPart;

	}

	/**
	 * Does something quite similar to isSolvable but is used by solve and
	 * solutionSpace
	 * 
	 * @param a
	 *            coefficient matrix
	 * @param b
	 *            result vector
	 * @return a.gaussjord() without zero rows or null if there is no solution
	 */

	private static Matrix isSolvableHelper(Matrix a, Vector b) {
		if (a.getRows() != b.length()) {
			throw new InvalidOperationException(
					"Tried to solve an equation system with a coefficient matrix"
							+ " with " + a.getRows() + " rows and a"
							+ " vector with length " + b.length()
							+ ". Not correct format!");
		}

		Matrix tmp = a.insertCol(a.getCols() + 1, b);
		// The Following is equivalent to: return tmp.rank() == a.rank();
		// But it is more efficient.

		tmp = tmp.gaussjord();
		int row = tmp.getRows();

		// Find the index of the first not zero row.
		while (row > 0 && tmp.isZeroRow(row)) {
			tmp = tmp.withoutRow(row);
			row--;
		}

		// Check whether there is any non zero entry in the first not zero row
		// of tmp
		// in a.gaussjord() (i.e. tmp without last column)
		for (int col = 1; col < tmp.getCols(); col++) {
			if (!tmp.get(row, col).isZero()) {
				return tmp;
			}
		}
		return null;
	}

	/**
	 * Tests whether a linear equation system (A*x=b) is solvable or not.
	 * 
	 * @param a
	 *            coefficient matrix
	 * @param b
	 *            result vector
	 * @return return true if and only if there is a solution for this linear
	 *         eqution system.
	 */

	public static boolean isSolvable(Matrix a, Vector b) {

		if (a.getRows() != b.length()) {
			throw new InvalidOperationException(
					"Tried to solve an equation system with a coefficient matrix"
							+ " with " + a.getRows() + " rows and a"
							+ " vector with length " + b.length()
							+ ". Not correct format!");
		}

		Matrix tmp = a.insertCol(a.getCols() + 1, b);
		// The Following is equivalent to: return tmp.rank() == a.rank();
		// But it is more efficient.

		tmp = tmp.gausselim();
		int row = tmp.getRows();

		// Find the index of the first not zero row.
		while (row > 0 && tmp.isZeroRow(row)) {
			row--;
		}

		// Check whether there is any non zero entry in the first not zero row
		// of tmp
		// in a.gaussjord() (i.e. tmp without last column)
		for (int col = 1; col < tmp.getCols(); col++) {
			if (!tmp.get(row, col).isZero()) {
				return true;
			}
		}

		return false;

	}

}
