package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

/**
 * This class represents an element of the modulo 2 field F2.
 * F2 is a field, just like the rational numbers are.
 * At first glance it seems to be a rather academic example,
 * but prime fields (especially F2) have numerous applications
 * (e.g.: error correcting codes).
 * @author Andreas Keilhauer
 */

public class F2 extends FieldElement implements Comparable {

	protected int value;

	/**
	 * Builds an element from an integer value.  The resulting value is 
	 * simply this integer value modulo 2.
	 */
	public F2(int value) {
		this.value = value % 2;
	}

	/**
	 * Calculates the sum of this element and another one.
	 *
	 * @param val
	 * @return sum <=> logical XOR
	 */
	public FieldElement add(FieldElement val) {
		F2 added = (F2) val;
		if ((this.value == 1 && added.value == 0)
			|| this.value == 0
			&& added.value == 1) {
			return new F2(1);
		}
		return new F2(0);
	}

	/**
	 * Calculates the difference between this element and another one.
	 *
	 * @param val
	 * @return difference <=> logical XOR
	 */
	public FieldElement subtract(FieldElement val) {
		F2 subtracted = (F2) val;
		if ((this.value == 1 && subtracted.value == 0)
			|| this.value == 0
			&& subtracted.value == 1) {
			return new F2(1);
		}
		return new F2(0);
	}

	/**
	 * Calculates the product of this element and another one.
	 *
	 * @param val
	 * @return product <=> logical AND 
	 */
	public FieldElement multiply(FieldElement val) {
		F2 factor = (F2) val;
		if (this.value == 1 && factor.value == 1) {
			return new F2(1);
		}
		return new F2(0);
	}

	/**
	 * Calculates the quotient of this FieldElement and another one.
	 *
	 * @param val
	 * @return quotient <=> this value if val = 1m2 and undefined (Exception) otherwise
	 * @throws InvalidOperationException if val = 0m2
	 */
	public FieldElement divide(FieldElement val) {
		F2 divisor = (F2) val;
		if (divisor.value == 1) {
			return new F2(this.value);
		}
		throw new InvalidOperationException("Division by 0");
	}

	/**
	 * Calculates the inverse element of addition for this element.
	 *
	 * @return negated <=> this value
	 */
	public FieldElement negate() {
		return new F2(this.value);
	}

	/**
	 * Calculates the inverse element of multiplication for this element.
	 *
	 * @return inverted (i.e., the value itself)
	 * @throws InvalidOperationException if original value is zero
	 */
	public FieldElement invert() throws InvalidOperationException {
		if (this.value == 0) {
			throw new InvalidOperationException("Division by 0");
		}
		return new F2(this.value);
	}

	/**
	 * Returns the neutral element of addition (zero element) of this
	 * FieldElement's field.
	 *
	 * @return zero
	 */
	public FieldElement zero() {
		return new F2(0);
	}

	/**
	 * Returns the neutral element of multiplication (one element) of
	 * this element's field.
	 *
	 * @return one
	 */
	public FieldElement one() {
		return new F2(1);
	}

	/**
	 * Checks two elements for equality.
	 *
	 * @param val
	 * @return true if the two FieldElements are mathematically equal.
	 */
	public boolean equals(FieldElement val) {
		F2 comp = (F2) val;
		return this.value == comp.value;
	}

	/**
	 * Returns a String representation of this element.
	 *
	 * @return String representation
	 */
	public String toString() {
		return this.value + "m2";
	}

	/**
	 * Implements Comparable.compareTo(Object).
	 * @param o the object
	 * @return {-,+,0} as this object is less than, equal to, or
	 * greater than the specified object.
	 */
	public int compareTo(Object o) {
		F2 comp = (F2) o;
		return (value < comp.value) ? -1 : (value > comp.value ? 1 : 0);
	}

	/**
	 * Returns an instance of this FieldElement to be used in computing mean
	 * and other values.
	 *
	 * @param dval the value to use
	 * @return instance
	 */
	public FieldElement instance(double dval) {
		return new F2((int) dval);
	}

	/**
	 * Changed standard implementation for mapRandomValue a little
	 * to ensure that both elements of F2 are equally likely in
	 * the uniformly distributed case - LinAlgFactory.randomValue()
	 * <BR><STRONG>Note!</STRONG> Normal Distribution Issue<BR>
	 * In case of a normally distributed double as in 
	 * LinAlgFactory.gaussianRandomValue() it won't give you the
	 * right distribution. 
	 * 
	 * @param dval
	 * @return mapped value
	 */
	public FieldElement mapRandomValue(double dval) {
		return instance(dval * 2);
	}
}
