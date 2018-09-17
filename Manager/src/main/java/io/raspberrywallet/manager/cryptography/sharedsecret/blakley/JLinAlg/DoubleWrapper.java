package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

/** 
 * This class wraps a double value and performs all FieldElement
 * operations on that double. Fast but not without rounding errors.
 * @author Andreas Keilhauer
 */

public class DoubleWrapper extends FieldElement {

	protected double value;

	/**
	 * Builds an element from a double-precision floating-point value.
	 */
	public DoubleWrapper(double value) {
		this.value = value;
	}
	
	public double getValue(){
		return this.value;
	}

	/**
	 * Calculates the sum of this element and another one.
	 *
	 * @param val
	 * @return sum
	 */
	public FieldElement add(FieldElement val) {
		DoubleWrapper added = (DoubleWrapper) val;
		return new DoubleWrapper(this.value + added.value);
	}

	/**
	 * Calculates the product of this element and another one.
	 *
	 * @param val
	 * @return product
	 */
	public FieldElement subtract(FieldElement val) {
		DoubleWrapper sub = (DoubleWrapper) val;
		return new DoubleWrapper(this.value - sub.value);
	}

	/**
	 * Calculates the product of this element and another one.
	 *
	 * @param val
	 * @return product
	 */
	public FieldElement multiply(FieldElement val) {
		DoubleWrapper mult = (DoubleWrapper) val;
		return new DoubleWrapper(this.value * mult.value);
	}

	/**
	 * Calculates the quotient of this element and another one.
	 *
	 * @param val
	 * @return this / val
	 */
	public FieldElement divide(FieldElement val) {
		DoubleWrapper div = (DoubleWrapper) val;
		return new DoubleWrapper(this.value / div.value);
	}

	/**
	 * Calculates the inverse element of addition for this element.
	 *
	 * @return negated (-value)
	 */
	public FieldElement negate() {
		return new DoubleWrapper(-this.value);
	}

	/**
	 * Calculates the inverse element of multiplication for this element.
	 *
	 * @return inverted (1/value)
	 * @throws InvalidOperationException if original value is zero
	 */
	public FieldElement invert() throws InvalidOperationException {
		if (this.isZero()) {
			throw new InvalidOperationException("Division by 0");
		}
		return new DoubleWrapper(1.0 / this.value);
	}

	/**
	 * Returns the neutral element of addition (zero element) of this
	 * FieldElement's field.
	 *
	 * @return zero
	 */
	public FieldElement zero() {
		return new DoubleWrapper(0);
	}

	/**
	 * Returns the neutral element of multiplication (one element) of
	 * this element's field.
	 *
	 * @return one
	 */
	public FieldElement one() {
		return new DoubleWrapper(1);
	}

	/**
	 * Checks two FieldElements for equality.
	 *
	 * @param obj
	 * @return true if the two FieldElements are mathematically equal.
	 */
	public boolean equals(Object obj) {
		DoubleWrapper comp = (DoubleWrapper) obj;
		return this.value == comp.value;
	}

	/**
	 * Returns a String representation of this element.
	 *
	 * @return String representation
	 */
	public String toString() {
		return "" + this.value;
	}

	/**
	 * Returns the double-precision floating-point value of this element.
	 *
	 * @return value
	 */
	public double doubleValue() {
		return this.value;
	}

	/**
	 * Implements Comparable.compareTo(Object).
	 * @param o the object
	 * @return {-,+,0} as this object is less than, equal to, or
	 * greater than the specified object.
	 */
	public int compareTo(Object o) {
		DoubleWrapper comp = (DoubleWrapper) o;
		return this.value < comp.value ? -1 : (this.value > comp.value ? 1 : 0);
	}

	/**
	 * Returns the square root of this value.
	 * @return sqrt(value)
	 */
	public double sqrt() {
		return Math.sqrt(this.value);
	}

	/**
	 * Returns an instance of this FieldElement to be used in computing mean
	 * and other values.
	 *
	 * @param dval the value to use
	 * @return instance
	 */

	public FieldElement instance(double dval) {
		return new DoubleWrapper(dval);
	}
	
	/**
	 * Returns a DoubleWrapper with wrapped value greater than or
	 * equal to 0.0 and less than 1.0 . Returned values are chosen
	 * pseudorandomly with (approximately) uniform distribution 
	 * from that range. 
	 */

	public FieldElement randomValue() {
		return new DoubleWrapper(Math.random());
	}

}
