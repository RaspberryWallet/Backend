package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.io.Serializable;

/**
 * This class represents a linear subspace.
 * 
 * @author Andreas Keilhauer
 */

public class LinearSubspace extends AffineLinearSubspace implements
		Serializable {

	/**
	 * This constructs a linear subspace with the given generating System.
	 * 
	 * @param generatingSystem
	 */

	public LinearSubspace(Vector[] generatingSystem) {
		super(null, generatingSystem);
	}
	
	/**
	 * This constructs a linear subspace with the given generating System.
	 * The normalized flag, which is usually set after
	 * normalize is executed is also set.
	 * 
	 * @param generatingSystem
	 * @param normalized
	 */
	
	public LinearSubspace(Vector[] generatingSystem, boolean normalized) {
		super(null, generatingSystem, normalized);
	}

	/**
	 * Returns a String representation of this linear subspace.
	 * 
	 * @return String representation
	 */
	public String toString() {
		String tmp = "< { ";
		for (int i = 0; i < this.generatingSystem.length - 1; i++) {
			tmp += this.generatingSystem[i].toString() + ", ";
		}
		if (this.generatingSystem.length > 0) {
			tmp += this.generatingSystem[this.generatingSystem.length - 1];
		}
		return tmp + " } >";

	}
}