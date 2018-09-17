package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

/**
 * The <i>MonadicOperator</i> interface supports application of
 * arbitrary monadic (one-argument) functions to the elements of a
 * Matrix or Vector, via the Matrix or Vector's <tt>apply</tt>
 * methods.
 *
 * @author Simon Levy
 */

public interface MonadicOperator {

	/**
	 * Applies the function to an element.
	 * @param x the value of the element
	 * @return the result of applying the function to <tt>x</tt>
	 */
	public FieldElement apply(FieldElement x);

}
