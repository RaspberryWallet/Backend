package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.math.BigInteger;

/**
 * This class represents a rational number with a numerator and a
 * denominator as BigInteger. The rational number will be kept
 * canceled all the time.  Furthermore complex numbers and rational
 * number are compatible in all operations.
 * @author Andreas Keilhauer, Simon D. Levy
 */

public class Rational extends FieldElement {

	protected BigInteger numerator;
	protected BigInteger denominator;

	/** 
	 * This constructor takes two longs as numerator and denominator.
	 * 
	 * @param numerator
	 * @param denominator
	 */

	public Rational(long numerator, long denominator) {
		this.numerator = BigInteger.valueOf(numerator);
		this.denominator = BigInteger.valueOf(denominator);
		cancel();
	}

	/**
	 * This creates a Rational with denominator 1
	 *
	 * @param value numerator
	 */

	public Rational(BigInteger value) {
		this.numerator = value;
		this.denominator = BigInteger.valueOf(1);
	}

	/**
	 * This creates a Rational.
	 *
	 * @param numerator
	 * @param denominator
	 */

	public Rational(BigInteger numerator, BigInteger denominator) {
		if ((numerator.equals(BigInteger.valueOf(0))
			&& !denominator.equals(BigInteger.valueOf(0)))) {
			this.numerator = numerator;
			this.denominator = BigInteger.valueOf(1);
		} else {
			this.numerator = numerator;
			this.denominator = denominator;
			cancel();
		}
	}

	/**
	 * This creates a Rational and lets you decide whether the
	 * rational still needs to be cancelled (normalized). Default
	 * is set to true.
	 *
	 * @param numerator
	 * @param denominator
	 */
	
	public Rational(
		BigInteger numerator,
		BigInteger denominator,
		boolean cancel) {
		this.numerator = numerator;
		this.denominator = denominator;

		if (cancel) {
			this.cancel();
		} else if (this.denominator.signum() == -1) {
			this.numerator = this.numerator.negate();
			this.denominator = this.denominator.negate();
		}
	}

	/**
	 * This creates a Rational out of a double. So for example 1.2
	 * will become 6/5 and Math.PI will become
	 * 3141592653589793/1000000000000000.
	 *
	 * @param value
	 */

	public Rational(double value) {

		boolean isNegative = false;
		if (value < 0) {
			isNegative = true;
			value = Math.abs(value);
		}

		String strValue = (new Double(value)).toString();

		int preDotDigits = strValue.indexOf(".");

		String preDotString = strValue.substring(0, preDotDigits);
		String postDotString = strValue.substring(preDotDigits + 1);

		int expDigitIndex = postDotString.indexOf("E");

		int exp = 0;

		if (expDigitIndex != -1) {
			exp = Integer.parseInt(postDotString.substring(expDigitIndex + 1));
			postDotString = postDotString.substring(0, expDigitIndex);
		}

		BigInteger newNumerator = new BigInteger(preDotString);
		BigInteger newNumerator2 = new BigInteger(postDotString);
		BigInteger newDenominator =
			BigInteger.valueOf((long) Math.pow(10, postDotString.length()));

		Rational tmp =
			(Rational) ((new Rational(newNumerator))
				.add(new Rational(newNumerator2, newDenominator)));

		tmp =
			(Rational) tmp.multiply(
				new Rational(BigInteger.valueOf((long) Math.pow(10, exp))));

		this.numerator = tmp.getNumerator();
		if (isNegative) {
			this.numerator = this.numerator.negate();
		}
		this.denominator = tmp.getDenominator();
	}

	/**
	 * Returns a String representation of this Rational.
	 */

	public String toString() {
		if (this.denominator.equals(BigInteger.valueOf(1))) {
			return numerator.toString();
		}
		return numerator + "/" + denominator;
	}

	/**
	 * Completely cancels this Rational.
	 */

	protected void cancel() {
		BigInteger cancelledBy = this.numerator.gcd(denominator);
		this.numerator = numerator.divide(cancelledBy);
		this.denominator = denominator.divide(cancelledBy);
		if (this.denominator.signum() == -1) {
			this.numerator = this.numerator.negate();
			this.denominator = this.denominator.negate();
		}
	}

	/**
	 * Returns the result of this Rational added to another one.
	 */

	public FieldElement add(FieldElement val) {
		if (val instanceof Complex) {
			Complex added = (Complex) val;
			return new Complex(
				(Rational) this.add(added.getReal()),
				added.getImaginary());
		}
		Rational added = (Rational) val;
		BigInteger a, b, c, d, gcdBD, p1, q1;
		a = this.numerator;
		b = this.denominator;
		c = added.getNumerator();
		d = added.getDenominator();
		gcdBD = b.gcd(d);
		p1 = a.multiply(d.divide(gcdBD)).add(c.multiply(b.divide(gcdBD)));
		q1 = b.multiply(d).divide(gcdBD);
		return new Rational(p1, q1);
	}

	/**
	 * Returns the result of this Rational multiplied with another one.
	 */

	public FieldElement multiply(FieldElement val) {
		if (val instanceof Complex) {
			Complex factor = (Complex) val;
			return new Complex(
				(Rational) this.multiply(factor.getReal()),
				(Rational) this.multiply(factor.getImaginary()));
		}
		Rational factor = (Rational) val;
		BigInteger d1 = this.numerator.gcd(factor.getDenominator());
		BigInteger d2 = this.denominator.gcd(factor.getNumerator());
		BigInteger newNumerator =
			this.numerator.divide(d1).multiply(
				factor.getNumerator().divide(d2));
		BigInteger newDenominator =
			this.denominator.divide(d2).multiply(
				factor.getDenominator().divide(d1));
		Rational tmp = new Rational(newNumerator, newDenominator, false);
		return tmp;
	}

	public FieldElement negate() {
		return new Rational(numerator.negate(), denominator, false);
	}

	/**
	 * Returns the inverted Rational of this rational. Therefore for
	 * 6/5 it will return 5/6.
	 */

	public FieldElement invert() throws InvalidOperationException {
		if (this.isZero()) {
			throw new InvalidOperationException("Division by 0");
		}
		return new Rational(this.getDenominator(), this.getNumerator(), false);
	}

	/**
	 * Returns the numerator of this Rational.
	 *
	 * @return numerator
	 */

	public BigInteger getNumerator() {
		return this.numerator;
	}

	/**
	 * Returns the denominator of this Rational.
	 *
	 * @return denominator
	 */

	public BigInteger getDenominator() {
		return this.denominator;
	}

	/**
	 * Returns the value of this Rational as a double.
	 *
	 * @return <code> numerator.doubleValue() /
	 * denominator.doubleValue();</code>
	 */

	public double doubleValue() {
		return numerator.doubleValue() / denominator.doubleValue();
	}

	/**
	 * Determines whether or not two Rationals are mathematically equal.
	 *
	 * @param obj another Rational/Complex
	 * @return Equal or not.
	 */

	public boolean equals(Object obj) {
		if (obj instanceof Complex) {
			Complex comp = (Complex) obj;
			if (!comp.imaginaryPart.isZero()) {
				return false;
			} else {
				return this.equals(comp.getReal());
			}
		}
		Rational comp = (Rational) obj;
		return (
			this.numerator.equals(comp.getNumerator())
				&& this.denominator.equals(comp.getDenominator()));
	}

	/**
	 * Returns the zero element for this field. 
	 *
	 * @return zero
	 */

	public FieldElement zero() {
		return new Rational(0);
	}

	/**
	 * Returns the one element for this field. 
	 *
	 * @return one
	 */

	public FieldElement one() {
		return new Rational(1);
	}

	/**
	 * Implements Comparable.compareTo(Object).
	 * @param o the object
	 * @return {-,+,0} as this object is less than, equal to, or
	 * greater than the specified object.
	 */

	public int compareTo(Object o) {
		Rational comp = (Rational) o;
		return ((Rational) this.subtract(comp)).getNumerator().signum();
	}

	/**
	 * Returns the absolute value of this Rational number.
	 *
	 * @return absolute value
	 */
	public FieldElement abs() {
		return new Rational(this.numerator.abs(), this.denominator, false);
	}

	/**
	 * Returns an instance of this FieldElement to be used in computing mean
	 * and other values.
	 *
	 * @param dval the value to use
	 * @return instance
	 */

	public FieldElement instance(double dval) {
		return new Rational(dval);
	}
		
}
