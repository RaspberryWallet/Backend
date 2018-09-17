package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.math.BigInteger;

/**
An immutable arbitrary precision binary float. Internally it is stored as two
BigIntegers, an exponent and a  mantissa. The number represented is
<code>mantissa * (2 ** exponent)</code>. The rightmost zeroes in the mantissa
are removed after all operations, with the exponent updated accordingly. The
length of the exponent is arbitrary. The leng of the mantissa is specified in
the field "mantissa_length", which defaults to 0x80. Changing this length will
only effect future operations.
@author Ryan Keppel
*/

public class BinaryFloat extends FieldElement {

	private final BigInteger mantissa;
	private final BigInteger exponent;
	public static int mantissa_length = 0x80;
	public FieldElement negate() {
		return new BinaryFloat(this.mantissa.negate(), this.exponent);
	}
	public FieldElement instance(double d) {
		return new BinaryFloat(d);
	}
	public FieldElement invert() {
		return new BinaryFloat(BigInteger.ONE).divide(this);
	}

	/**
	 * Returns the zero element for this field. 
	 *
	 * @return zero
	 */

	public FieldElement zero() {
		return new BinaryFloat(BigInteger.ZERO);
	}

	/**
	 * Returns the one element for this field. 
	 *
	 * @return one
	 */

	public FieldElement one() {
		return new BinaryFloat(BigInteger.ONE);
	}
	/**
	 * Implements Comparable.compareTo(Object).
	 * @param o the object
	 * @return {-,+,0} as this object is less than, equal to, or
	 * greater than the specified object.
	 */

	public int compareTo(Object o) {
		return compareTo((BinaryFloat) o);
	}

	private BigInteger shiftRight(BigInteger b, int i) {
		BigInteger rt = b.abs().shiftRight(i);
		if (b.signum() == -1)
			rt = rt.negate();
		return rt;
	}

	private BigInteger shiftLeft(BigInteger b, int i) {
		BigInteger rt = b.abs().shiftLeft(i);
		if (b.signum() == -1)
			rt = rt.negate();
		return rt;
	}

	private static int bigToInt(BigInteger b) {
		if (b.bitLength() > 0x1F)
			throw new Error();
		return b.intValue();
	}

	private BigInteger invRemainder(BigInteger b1, BigInteger b2) {
		BigInteger b;
		b = b1.remainder(b2);
		if (b.signum() == 0)
			return b;
		else
			return b2.subtract(b);
	}

	/*
	public BinaryFloat(BigInteger mantissa, BigInteger exponent)
	Hexadecimal constructor
	{
	  this.mantissa=mantissa;
	  exponent=exponent.multiply(new BigInteger("4"));
	  exponent=exponent.subtract(invRemainder(
	BigInteger.valueOf(mantissa.abs().bitLength()),new BigInteger("4")) );
	  this.exponent=exponent;
	  normal();
	}
	*/
	/**
	Creates with double.
	*/
	public BinaryFloat(double d) {
		long a;
		BigInteger man, exp;
		BinaryFloat rt;
		a = Double.doubleToRawLongBits(d);
		if (a == 0) {
			this.mantissa = BigInteger.ZERO;
			this.exponent = BigInteger.ZERO;
		} else {
			man = BigInteger.ONE;
			if ((a >>> 0x3f) == 1) {
				man = man.negate();
				a -= 1L << 0x3f;
			}
			exp = BigInteger.valueOf((a >>> 0x34) - 0x3ff - 0x34);
			a &= ((1L << 0x34) - 1);
			a += 1L << 0x34;
			man = man.multiply(BigInteger.valueOf(a));
			rt = new BinaryFloat(man, exp);
			this.exponent = rt.exponent;
			this.mantissa = rt.mantissa;
		}
	}

	public double toDouble() {
		long a;
		double rt;
		BinaryFloat bf;
		BigInteger man, exp;
		a = 0;
		if (this.mantissa.signum() == 0)
			return 0;
		if (this.mantissa.signum() == -1)
			a += 1L << 0x3f;
		bf = this.newMantissaLength(0x35);
		man = bf.mantissa.abs();
		exp =
			bf.exponent.add(BigInteger.valueOf(bf.mantissa.abs().bitLength()));
		exp = exp.add(new BigInteger("3FE", 0x10));
		man = man.shiftLeft(0x35 - man.bitLength());
		a += man.clearBit(0x34).longValue();
		if ((exp.signum() == -1)
			|| (exp.compareTo(new BigInteger("800", 0x10)) != -1))
			throw new Error();
		a += (long) bigToInt(exp) << 0x34;
		return Double.longBitsToDouble(a);
	}

	/**
	* This creates a BinaryFloat with a given radix. The number entered is
	0.mantissa * (radix ** exponent)
	*/
	public BinaryFloat(BigInteger mantissa, int exponent, int radix) {
		BinaryFloat rt, b;
		long temp;
		temp = exponent;
		temp -= mantissa.toString(radix).length();
		exponent = (int) temp;
		if (exponent != temp)
			throw new Error();
		rt = new BinaryFloat(mantissa);
		if (exponent > 0) {
			rt =
				(BinaryFloat) rt.multiply(
					new BinaryFloat(BigInteger.valueOf(radix).pow(exponent)));
		} else if (exponent < 0) {
			rt =
				(BinaryFloat) rt.divide(
					new BinaryFloat(BigInteger.valueOf(radix).pow(-exponent)));
		}
		rt = rt.newMantissaLength(mantissa_length);
		this.mantissa = rt.mantissa;
		this.exponent = rt.exponent;
	}
	/**
	Creates a new BinaryFloat equal to BigInteger b.
	@param b -
	*/
	public BinaryFloat(BigInteger b) {
		this(b, BigInteger.ZERO);
	}

	/**
	Returns truncated BinaryFloat as a BigInteger.
	@return a BigInteger representing this truncated.
	*/
	public BigInteger toBigInteger() {
		return shiftLeft(mantissa, bigToInt(exponent));
	}

	/**Basic constructor.  Number represented is mantissa * (2 raised to
	exponent).
	@param mantissa -
	@param exponent -*/

	public BinaryFloat(BigInteger mantissa, BigInteger exponent) {
		this.exponent =
			exponent.add(BigInteger.valueOf(mantissa.getLowestSetBit()));
		this.mantissa = shiftRight(mantissa, mantissa.getLowestSetBit());
	}

	/**
	* The standard output gives a hexadecimal representation. The exponent symbol
	is "P".
	@return a String representing this.
	*/
	public String toString() {
		String rt = "";
		BigInteger man, exp;
		if (this.mantissa.signum() == 0)
			return "0";
		exp = exponent.add(BigInteger.valueOf(mantissa.abs().bitLength()));
		BigInteger remain = exp.remainder(new BigInteger("4"));
		if (this.mantissa.signum() == -1)
			rt = "-";
		man = this.mantissa.abs();
		exp = exp.divide(new BigInteger("4"));
		if (remain.signum() == 1)
			exp = exp.add(BigInteger.ONE);
		man =
			shiftLeft(
				man,
				invRemainder(
					BigInteger.valueOf(man.abs().bitLength()).add(
						new BigInteger("4")).subtract(
						remain),
					new BigInteger("4"))
					.intValue());
		rt += "0x0.";
		rt += man.toString(0x10);
		rt += "P";
		rt += exp.toString(0x10);
		return rt;
	}
	/**
	This outputs the raw mantissa and exponent, good for debugging.
	*/
	public String toBinString() {
		String rt = "";
		if (this.mantissa.signum() == 0)
			return "0";
		rt += this.mantissa.toString(0x2);
		rt += "Pbin";
		rt += this.exponent.toString(0x2);
		return rt;
	}
	/**
	Returns a string representing this in the given radix. The length is the
	maximum digits (in the specified radix) to give.
	@param radix - From 2 to 36 (24h)
	@param length - 
	*/
	public String toString(int radix, int length) {
		String rt = "", manr;
		BigInteger man, exp;
		BinaryFloat bf;
		int expsign = exponent.signum();
		int i = 0;
		int result;
		if (length < 1)
			throw new Error();
		if (this.mantissa.signum() == 0)
			return "0";
		if (this.mantissa.signum() == -1)
			rt = "-";
		rt += "0.";
		if (expsign >= 0) {
			man = this.toBigInteger();
			manr = man.abs().toString(radix);
		} else {
			exp = exponent.abs();
			while ((result =
				BigInteger.valueOf(radix).pow(-i).compareTo(
					BigInteger.ONE.shiftLeft(bigToInt(exp))))
				< 1)
				i--;
			if (result != 0)
				i--;
			bf =
				(BinaryFloat) this.multiply(
					new BinaryFloat(BigInteger.valueOf(radix).pow(-i)));
			man = shiftLeft(bf.mantissa, bigToInt(bf.exponent));
			manr = man.abs().toString(radix);
			while ((manr.length() <= length) && (bf.exponent.signum() == -1)) {
				bf =
					(BinaryFloat) bf.multiply(
						new BinaryFloat(BigInteger.valueOf(radix)));
				i--;
				man = shiftLeft(bf.mantissa, bigToInt(bf.exponent));
				manr = man.abs().toString(radix);
			}
		}
		i += manr.length();
		if (manr.length() > length)
			rt += "~" + manr.substring(0, length);
		else
			rt += manr;
		while (rt.charAt(rt.length() - 1) == '0')
			rt = rt.substring(0, rt.length() - 1);
		rt += "(Exponent)";
		rt += Integer.toString(i, radix);
		return rt;
	}
	/**
	Returns a new BinaryFloat with the given length as a maximum.
	@param length -
	*/
	public BinaryFloat newMantissaLength(int length) {
		BinaryFloat rt = new BinaryFloat(BigInteger.ZERO);
		if (length < 1)
			throw new Error();
		if (length >= mantissa.abs().bitLength())
			return this;
		rt =
			new BinaryFloat(
				shiftRight(mantissa, mantissa.abs().bitLength() - length),
				exponent.add(
					BigInteger.valueOf(mantissa.abs().bitLength() - length)));
		return rt;
	}
	/**
	Compares this with specified.
	@return -1, 0, or 1 as this is, respectively, less than, equal to, or greater
	than.
	*/
	public int compareTo(BinaryFloat b) {
		return ((BinaryFloat) (this.subtract(b))).mantissa.signum();
	} /**
	Addition.
	*/
	public FieldElement add(FieldElement fe) {
		BinaryFloat b = (BinaryFloat) fe;
		BinaryFloat rt, temp;
		BigInteger manx, many, expx, expy, manr, expr;
		int lengthman, lengthexp;
		if (b.mantissa.signum() == 0)
			return this;
		if (this.mantissa.signum() == 0)
			return b;
		manx = this.mantissa;
		many = b.mantissa;
		expx = this.exponent;
		expy = b.exponent;
		if (expx.compareTo(expy) == 1)
			manx = manx.shiftLeft(bigToInt(expx.subtract(expy)));
		else {
			many = many.shiftLeft(bigToInt(expy.subtract(expx)));
			expy = expx;
		}
		expr = expy;
		manr = manx.add(many);
		if (manr.signum() == 0)
			return new BinaryFloat(BigInteger.ZERO);
		rt = new BinaryFloat(manr, expr);
		rt = newMantissaLength(mantissa_length);
		return rt;
	}
	/**
	Multiplication
	*/
	public FieldElement multiply(FieldElement fe) {
		BinaryFloat b = (BinaryFloat) fe;
		BinaryFloat rt;
		if ((this.mantissa.signum() * b.mantissa.signum()) == 0)
			return new BinaryFloat(BigInteger.ZERO);
		return new BinaryFloat(
			this.mantissa.multiply(b.mantissa),
			this.exponent.add(b.exponent));
	}

	/**
	* Divide by the given BinaryFloat, using a maximum of i bits of precision in
	the result.
	*/
	public FieldElement divide(FieldElement fe) {
		BinaryFloat b = (BinaryFloat) fe;
		int i = mantissa_length;
		BinaryFloat rt;
		BigInteger manr, expr, remainder;
		if (i < 1)
			throw new Error();
		if (b.mantissa.signum() == 0)
			throw new Error();
		if (this.mantissa.signum() == 0)
			return new BinaryFloat(BigInteger.ZERO);
		expr = this.exponent.subtract(b.exponent);
		manr = this.mantissa.divide(b.mantissa);
		remainder = this.mantissa.abs().remainder(b.mantissa.abs());
		while ((remainder.signum() == 1) && (manr.bitLength() < i)) {
			manr = manr.shiftLeft(1);
			expr = expr.subtract(BigInteger.ONE);
			remainder = remainder.shiftLeft(1);
			if (remainder.compareTo(b.mantissa) >= 0) {
				remainder = remainder.subtract(b.mantissa);
				manr = manr.setBit(0);
			}
		}
		rt = new BinaryFloat(manr, expr);
		rt = rt.newMantissaLength(i);
		return rt;
	}
	/**
	Returns a BinaryFloat whose value is <code>this**a</code>.
	*/
	public FieldElement pow(int a) {
		BinaryFloat rt;
		if (mantissa.signum() == 0)
			if (a == 0)
				throw new Error();
			else
				return new BinaryFloat(BigInteger.ZERO);
		if (a == 0)
			return new BinaryFloat(BigInteger.ONE);
		rt =
			new BinaryFloat(
				this.mantissa.pow(Math.abs(a)),
				this.exponent.multiply(BigInteger.valueOf(Math.abs(a))));
		if (a < 0)
			rt = (BinaryFloat) new BinaryFloat(BigInteger.ONE).divide(rt);
		return rt;
	}
	/*
	public static void main (String args[])
	{
	  BinaryFloat bf=new BinaryFloat(new BigInteger("318728478278732872873"),new
	BigInteger("-2",0x10));
	  BinaryFloat bf1=new BinaryFloat(new BigInteger("1232983753"),2,0x1000,0xA);
	  BinaryFloat bf2=new BinaryFloat(new
	BigInteger("23787821783"),0,0x1100,0xA);
	  System.out.println(bf1.add(bf2).toString(0xA,0x100));
	  System.out.println(bf1.sub(bf2).toString(0xA,0x100));
	  System.out.println(bf1.mul(bf2).toString(0xA,0x100));
	  System.out.println(bf1.div(bf2,0x100).toString(0xA,0x100));
	  System.out.println(bf1.pow(-5).toString(0xA,0x10000));
	
	  System.out.println(bf.toString(0xA,0x1000));
	  System.out.println(bf.toBinString());
	  System.out.println(bf.toDouble());
	}*/
}
