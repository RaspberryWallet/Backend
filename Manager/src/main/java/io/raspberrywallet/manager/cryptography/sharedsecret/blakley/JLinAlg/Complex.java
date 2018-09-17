package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

/**
 * This class represents a complex number with two Rationals as a
 * real- and imaginary part.  Therefore there will not be any rounding
 * errors.  Furthermore complex numbers and rational number are
 * compatible in all operations.
 * @author Andreas Keilhauer, Simon D. Levy
 */

public class Complex extends FieldElement {

	/**
	 * The real part of this Complex as a Rational.
	 */

	protected Rational realPart;

	/**
	 * The imaginary part of this Complex as a Rational.
	 */

	protected Rational imaginaryPart;

	/**
	 * Creates a Complex out of two Rationals, representing real- and
	 * imaginary part.
	 *
	 * @param realPart
	 * @param imaginaryPart
	 */

	public Complex(Rational realPart, Rational imaginaryPart) {
		this.realPart = realPart;
		this.imaginaryPart = imaginaryPart;
	}

	/**
	 * Creates a Complex out of two doubles, representing real- and
	 * imaginary part.
	 *
	 * @param realPart
	 * @param imaginaryPart
	 */

	public Complex(double realPart, double imaginaryPart) {
		this.realPart = new Rational(realPart);
		this.imaginaryPart = new Rational(imaginaryPart);
	}

	/**
	 * Gets the real part of this complex.
	 *
	 * @return real part as a Rational
	 */

	public Rational getReal() {
		return realPart;
	}

	/**
	 * Gets the imaginary part of this complex.
	 *
	 * @return imaginary part as a Rational
	 */

	public Rational getImaginary() {
		return imaginaryPart;
	}

	/**
	 * Returns the result of this Complex added to another one.
	 */

	public FieldElement add(FieldElement val) {
		if (val instanceof Rational) {
			return new Complex(
				(Rational) (this.realPart.add(val)),
				this.imaginaryPart);
		}
		Complex added = (Complex) val;
		Rational newRealPart = (Rational) this.realPart.add(added.getReal());
		Rational newImaginaryPart =
			(Rational) this.imaginaryPart.add(added.getImaginary());
		return new Complex(newRealPart, newImaginaryPart);
	}

	/**
	 * Returns the result of this Complex multiplied with another one.
	 */

	public FieldElement multiply(FieldElement val) {
		if (val instanceof Rational) {
			return new Complex(
				(Rational) (this.realPart.multiply(val)),
				(Rational) (this.imaginaryPart.multiply(val)));
		}
		Complex mult = (Complex) val;
		Rational newRealPart =
			(Rational) (this.realPart.multiply(mult.getReal())).subtract(
				this.imaginaryPart.multiply(mult.getImaginary()));
		Rational newImaginaryPart =
			(Rational) (this.realPart.multiply(mult.getImaginary())).add(
				this.imaginaryPart.multiply(mult.getReal()));
		return new Complex(newRealPart, newImaginaryPart);
	}

	public FieldElement negate() {
		return new Complex(
			(Rational) realPart.negate(),
			(Rational) imaginaryPart.negate());
	}

	public FieldElement invert() throws InvalidOperationException {
		if (this.isZero()) {
			throw new InvalidOperationException("Division by 0");
		}
		Rational normalize =
			(Rational) this.realPart.multiply(this.realPart).add(
				this.imaginaryPart.multiply(this.imaginaryPart));
		return new Complex(
			(Rational) this.realPart.divide(normalize),
			(Rational) this.imaginaryPart.negate().divide(normalize));
	}

	/**
	 * Returns a Complex that is this Complex conjugated.
	 *
	 * @return this Complex conjugated 
	 */

	public Complex conjugate() {
		return new Complex(realPart, (Rational) imaginaryPart.negate());
	}

	/**
	 * Determines whether two Complex numbers are mathematically equal.
	 *
	 * @param obj another Complex/Rational
	 * @return true if and only if the real- as well as the imaginary
	 * parts are equal.
	 */

	public boolean equals(Object obj) {
		if (obj instanceof Rational) {
			Rational comp = (Rational) obj;
			if (!this.imaginaryPart.isZero()) {
				return false;
			} else {
				return this.realPart.equals(comp);
			}
		}
		Complex comp = (Complex) obj;
		return this.realPart.equals(comp.getReal())
			&& this.imaginaryPart.equals(comp.getImaginary());
	}

	/**
	 * Returns the neutral element of addition.
	 *
	 * @return 0 + 0 * i
	 */

	public FieldElement zero() {
		return new Complex(new Rational(0), new Rational(0));
	}

	/**
	 * Returns the neutral element of multiplication.
	 *
	 * @return 1 + 0 * i
	 */

	public FieldElement one() {
		return new Complex(new Rational(1), new Rational(0));
	}

	/**
	 * Returns a String representation of this Complex.
	 */

	public String toString() {
		String tmp = "";

		boolean reIsZero = this.realPart.isZero();
		boolean imAbsIsOne = this.imaginaryPart.abs().isOne();
		int compImZero = this.imaginaryPart.compareTo(new Rational(0));

		if (!reIsZero || (reIsZero && compImZero == 0)) {
			tmp += this.realPart.toString();
		}

		if (compImZero != 0) {
			if (compImZero > 0 && !reIsZero) {
				tmp += " + ";
			} else if (compImZero < 0) {
				tmp += " - ";
			}

			if (!imAbsIsOne) {
				tmp += imaginaryPart.abs().toString() + " ";
			}
			tmp += "i";
		}

		return tmp;
	}

	/**
	 * Implements Comparable.compareTo(Object).  Comparison is based on 
	 * Rational magnitude value.
	 * @param o the object
	 * @return {-,+,0} as this object is less than, equal to, or
	 * greater than the specified object.
	 */

	public int compareTo(Object o) {
		Complex comp = (Complex) o;
		return this.magnitude().compareTo(comp.magnitude());
	}

	/**
	 * Returns magnitude (sum of squares of real and imaginary components) of
	 * this Complex number.
	 * @return magnitude
	 */

	public Rational magnitude() {
		Rational a = getReal();
		Rational b = getImaginary();
		return (Rational) b.multiply(b).add(a.multiply(a));
	}

	/**
	 * Returns an instance of this FieldElement to be used in computing mean
	 * and other values.
	 *
	 * @param dval the value to use
	 * @return instance
	 */

	public FieldElement instance(double dval) {
		return new Complex(dval, 0);
	}
	
}
