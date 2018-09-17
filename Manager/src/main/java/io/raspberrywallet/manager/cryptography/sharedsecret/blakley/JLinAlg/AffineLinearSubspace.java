package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * This class represents an affin linear subspace.
 * 
 * @author Andreas Keilhauer
 */

public class AffineLinearSubspace implements Serializable {

	protected Vector inhomogenousPart = null;

	protected Vector[] generatingSystem = null;

	protected int dimension = -1;

	protected boolean normalized = false;

	/**
	 * This creates an affin linear subspace by taking an inhomogenous part and
	 * a generating System of Vectors. The subspace will be inhomogenousPart + <
	 * generatingSystem >.
	 * 
	 * @param inhomogenousPart
	 * @param generatingSystem
	 */

	public AffineLinearSubspace(Vector inhomogenousPart,
			Vector[] generatingSystem) throws InvalidOperationException {

		if (generatingSystem != null && generatingSystem.length > 0) {
			this.generatingSystem = generatingSystem;
		} else {
			this.generatingSystem = new Vector[0];
		}

		if (inhomogenousPart == null) {
			Vector tmp = generatingSystem[0];
			Vector zeroVector = new Vector(tmp.length());
			FieldElement zero = tmp.getEntry(1).zero();
			for (int i = 1; i <= zeroVector.length(); i++) {
				zeroVector.set(i, zero);
			}
			this.inhomogenousPart = zeroVector;
		} else {
			this.inhomogenousPart = inhomogenousPart;
		}
	}

	/**
	 * This creates an affin linear subspace by taking an inhomogenous part and
	 * a generating System of Vectors. The subspace will be inhomogenousPart + <
	 * generatingSystem >. The normalized flag, which is usually set after
	 * normalize is executed, is also set, but it won't be checked.
	 * 
	 * @param inhomogenousPart
	 * @param generatingSystem
	 * @param normalized
	 */

	public AffineLinearSubspace(Vector inhomogenousPart,
			Vector[] generatingSystem, boolean normalized)
			throws InvalidOperationException {

		this.normalized = true;

		if (generatingSystem != null && generatingSystem.length > 0) {
			this.generatingSystem = generatingSystem;
			if (normalized) {
				this.dimension = generatingSystem.length;
			}
		} else {
			this.generatingSystem = new Vector[0];
		}

		if (inhomogenousPart == null) {
			Vector tmp = generatingSystem[0];
			Vector zeroVector = new Vector(tmp.length());
			FieldElement zero = tmp.getEntry(1).zero();
			for (int i = 1; i <= zeroVector.length(); i++) {
				zeroVector.set(i, zero);
			}
			this.inhomogenousPart = zeroVector;
		} else {
			this.inhomogenousPart = inhomogenousPart;
		}
	}

	/**
	 * Gets the dimension of the affin linear subspace.
	 * 
	 * @return dimension (number of independent Vectors of the generating
	 *         system).
	 */

	public int getDimension() {
		if (this.normalized) {
			return this.dimension;
		} else {
			return (new Matrix(generatingSystem).rank());
		}

	}

	/**
	 * Gets the inhomogenous part of this affin linear vector space.
	 * 
	 * @return inhomogenous part
	 */

	public Vector getInhomogenousPart() {
		return inhomogenousPart;
	}

	/**
	 * Gets the generating system of this affin linear vector space.
	 * 
	 * @return generating system
	 */

	public Vector[] getGeneratingSystem() {
		return generatingSystem;
	}

	/**
	 * Returns a String representation of this affine linear subspace
	 * 
	 * @return String representation
	 */

	public String toString() {
		String tmp = this.inhomogenousPart + " + < { ";
		for (int i = 0; i < this.generatingSystem.length - 1; i++) {
			tmp += this.generatingSystem[i].toString() + ", ";
		}
		if (this.generatingSystem.length > 0) {
			tmp += this.generatingSystem[this.generatingSystem.length - 1];
		}
		return tmp + " } >";
	}

	/**
	 * This method calculates the normalized version of this AffineLinearSubspace.
	 * I.e. it eliminates all dependent vectors from the generating System.
	 * In a 1-dimensional AffineLinearSubspace it is also possible,
	 * that the inhomogenous part is dependent to the generating vector and
	 * therefore the inhomogenous can be dropped.
	 * 
	 * @return the normalized version of this
	 */
	public AffineLinearSubspace normalize() {
		if (this.generatingSystem.length > 0) {
			Matrix normalized = new Matrix(generatingSystem).gausselim();
			LinkedList generatingVectors = new LinkedList();
			int i = 1;
			while (i < normalized.getRows() && !normalized.isZeroRow(i)) {
				generatingVectors.addLast(normalized.getRow(i));
				i++;
			}
			Vector[] newGeneratingSystem = (Vector[]) generatingVectors
					.toArray(new Vector[0]);
			
			if(newGeneratingSystem.length == 1){
				Matrix testInhomogenousPart = new Matrix(new Vector[]{newGeneratingSystem[0], this.inhomogenousPart});
				if(testInhomogenousPart.rank() != 2){
					return new LinearSubspace(newGeneratingSystem);
				}
			}
			if (this instanceof LinearSubspace) {
				return new LinearSubspace(newGeneratingSystem, true);
			} else {
				return new AffineLinearSubspace(this.inhomogenousPart,
						newGeneratingSystem, true);
			}

		}
		
		return this;
	}

	/**
	 * @return the value of the normalized flag
	 */
	public boolean isNormalized() {
		return this.normalized;
	}

}