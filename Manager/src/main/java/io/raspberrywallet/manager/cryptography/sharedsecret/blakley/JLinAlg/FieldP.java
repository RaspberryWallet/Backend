package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Random;

/**
 * This class represents an element of the modulo p field Fp (i.e. the Galois field GF(p))
 * where p is prime. Fp is a field like any other field
 * but there is no order that respects addition.
 *  
 * @author Andreas Lochbihler
 */
/* This is only a wrapper class for the different implementations for FieldPAbstract
 * depending on the magnitude of p.
 * */
public class FieldP extends FieldElement {

	/** for all primes p less than PRIME_SEPARATION_BOUNDARY long
	 * variables are sufficient for computation whereas above BigInteger
	 * must be used. */
	private static final long PRIME_SEPARATION_BOUNDARY = 3037000500l;
	private static final BigInteger PRIME_SEPARATION_BOUNDARY_BIG =
		BigInteger.valueOf(PRIME_SEPARATION_BOUNDARY);

	/** for all Fp with p &lt;= inversesLookupTableBoundary inverses are stored
	 * in a lookup table of size p , for bigger fields inverses are stored with the
	 * element. It is assumed that inversesLookupTableBoundary < PRIME_SEPARATION_BOUNDARY */
	private static long inversesLookupTableBoundary = 65521;

	/** Reference to the implementation of this element */
	private FieldPAbstract element;

	/**
	 * Creates a new FieldP object representing the element (equivalence class) of
	 * the field Fp which contains value. Depending on the magnitude of p,
	 * the right implementations is automatically chosen. See setInversesLookupTableBoundary
	 * for tuning options. 
	 * @param value A representative of the equivalence class to create
	 * @param p The positive prime specifying the number of elements in Fp.
	 */
	public FieldP(long value, long p) {
		if (!isPrime(p)) {
			throw new IllegalArgumentException("p = " + p + " is not a prime.");
		}

		if (p <= inversesLookupTableBoundary) {
			element = FieldPLongLookup.instance(value, p);
		} else if (p < PRIME_SEPARATION_BOUNDARY) {
			element = new FieldPLongNoLookup(value, p);
		} else {
			element =
				new FieldPBig(BigInteger.valueOf(value), BigInteger.valueOf(p));
		}
	}

	/**
	 * Creates a new FieldP object representing the element (equivalence class) of
	 * the field FP which contains value. Depending on the magnitude of p,
	 * the right implementation is automatically chosen, i.e., if p is small enough
	 * to do all computations using long, this implementation will be chosen.
	 * @param value A representative of the equivalence class to create.
	 * @param p The positive prime specifying the number of elements in Fp.
	 */
	public FieldP(BigInteger value, BigInteger p) {
		if (!isPrime(p)) {
			throw new IllegalArgumentException("p = " + p + " is not a prime.");
		}

		if (p.compareTo(PRIME_SEPARATION_BOUNDARY_BIG) < 0) {
			long pL = p.longValue();
			long vL = value.mod(p).longValue();

			if (pL <= inversesLookupTableBoundary) {
				element = FieldPLongLookup.instance(vL, pL);
			} else {
				element = new FieldPLongNoLookup(vL, pL);
			}
		} else {
			element = new FieldPBig(value, p);
		}
	}

	/**
	 * Creates a new wrapper object with reference to element. For internal use only.
	 * @param element The element to be represented by the new wrapper object.
	 */
	private FieldP(FieldPAbstract element) {
		this.element = element;
	}

	/**
	 * Adds this element to val and returns the new element of Fp.
	 * @param val The second operand. Must be of the same field Fp.
	 * @return The element that is the sum of this and val.
	 */
	public FieldElement add(FieldElement val) {
		return new FieldP((FieldPAbstract) element.add(((FieldP) val).element));
	}

	/**
	 * Multiplies this element with val and returns the new element of Fp.
	 * @param val The second operand. Must be of the same field Fp.
	 * @return The element that is the sum of this and val.
	 */
	public FieldElement multiply(FieldElement val) {
		return new FieldP(
			(FieldPAbstract) element.multiply(((FieldP) val).element));
	}

	/**
	 * Returns the zero element of this field Fp.
	 * @return The zero element of this field Fp.
	 */
	public FieldElement zero() {
		return new FieldP((FieldPAbstract) element.zero());
	}

	/**
	 * Returns the one element of this field Fp.
	 * @return The one element of this field Fp.
	 */
	public FieldElement one() {
		return new FieldP((FieldPAbstract) element.one());
	}

	/**
	 * Returns the additive inverse of this in the field Fp.
	 * @return The additive inverse of this in the field Fp.
	 */
	public FieldElement negate() {
		return new FieldP((FieldPAbstract) element.negate());
	}

	/**
	 * Returns the multiplicative inverse of this in the field Fp.
	 * @return The multiplicative inverse of this in the field Fp.
	 */
	public FieldElement invert() {
		return new FieldP((FieldPAbstract) element.invert());
	}

	/**
	 * Compares this element with another element. o must be a instance of
	 * FieldP and its field must have the same number of elements as this' one.
	 * A element of Fp is considered to be smaller than a other element if and
	 * only if its smallest nonnegative representative is smaller than the other's one.
	 * @param o The object to compare to. Must be a instance of FieldP and of the same
	 *          field Fp as this.
	 * @return &lt; 0 if this is smaller than o,
	 *         = 0 if this is equal to o (in the sense of equals),
	 *         &gt; 0 if this is bigger than o
	 */
	public int compareTo(Object o) {
		return element.compareTo(((FieldP) o).element);
	}

	/**
	 * Computes the element for the integral part of dval
	 * @param dval The value to use
	 * @return The element representing the integral part of dval
	 */
	public FieldElement instance(double dval) {
		return element.instance(dval);
	}

	/**
	 * Maps a double value (0<=dval<1) to a element of FieldP<BR>
	 * <STRONG>Note!</STRONG> Normal Distribution Issue<BR> 
	 * In case of a normally distributed double as in 
	 * LinAlgFactory.gaussianRandomValue() it won't give you 
	 * the right distribution.
	 * @param random The random value to be mapped
	 * @return a random element corresponding to dval.nextDouble().
	 */
	protected FieldElement randomValue(Random random) {
		return new FieldP((FieldPAbstract) element.randomValue(random));
	}

	protected FieldElement gaussianRandomValue(Random random) {
		return new FieldP((FieldPAbstract) element.randomValue(random));
	}

	/**
	 * Returns true if and only if this and o represent the same element of the same field.
	 * @param fe The element to compare to.
	 * @return True if and only if this and o are the same equivalence class of the same field.
	 */
	public boolean equals(FieldElement fe) {
		return element.equals(((FieldP) fe).element);
	}

	/**
	 * Returns a string representation of this element of the form vmp where
	 * v is the smallest non-negative representant of the equivalence class in the
	 * field with p elements.
	 * @return The string representation of this element.  
	 */
	public String toString() {
		return element.toString();
	}

	/**
	 * Returns true if and only if this and o represent the same element of the same field.
	 * @param o The element to compare to.
	 * @return True if and only if this and o are the same equivalence class of the same field.
	 */
	public boolean equals(Object o) {
		return element.equals(((FieldP) o).element);
	}

	/**
	 * Returns the number n which decides whether to store inverses in a lookup table
	 * (for fields with less or equal than n elements) or with the elements (otherwise)
	 * @return The number inversesLookupTableBoundary
	 */
	public static long getInversesLookupTableBoundary() {
		return inversesLookupTableBoundary;
	}

	/**
	 * This methods sets the boundary on the elements of Fp above which all inverses
	 * are stored with the elements and therefore may be computed several times instead
	 * of storing them in a lookup table. When using a lookup table for the inverses
	 * the inverse of a element will be automatically computed at creation time and stored
	 * in the table. If you are sure you will never need division it might be faster to
	 * set the boundary lower than the number of elements in your field BEFORE creating the
	 * first element of it. 
	 * 
	 * WARNING: If you change this boundary AFTER you have instantiated at least one
	 * element of Fp where p is less than Integer.MAX_VALUE, all existing elements
	 * which have not used the lookup-table won't use it either afterwards. Moreover,
	 * any new elements generated from these by unary operations or by binary operations
	 * where they from the first operand won't use it either. Similarily all elements which
	 * have used the lookup table and all elements generated from them in the same way will
	 * still use the lookup table. The memory allocated to the lookup table will not be freed
	 * before all elements using the lookup table are removed. Lookup tables are separate for
	 * each Fp. 
	 * 
	 * @param boundary The number n of elements in Fp above which no lookup table should be
	 * used.
	 */
	public static void setInversesLookupTableBoundary(long boundary) {
		inversesLookupTableBoundary = Math.min(Integer.MAX_VALUE, boundary);
	}

	/**
	 * Tuning option. If you expect to need most of the inverses in Fp, i.e. want to
	 * divide by most of the elements, then this speeds the division up by computing
	 * the inverses ahead.
	 * This method only computes the inverse if there is a lookup table for the inverses.
	 */
	public void computeAllInverses() {
		element.computeAllInverses();
	}

	/**
	 * Checks for primality of p. Intended to be used for checking the primality
	 * requirement of p in a Fp field. Not implemented yet, it is left up to the
	 * user to ensure the primality of p.
	 * @param p The number to check for primality
	 * @return For the time being, 2 and all odd numbers are considered prime
	 */
	static boolean isPrime(long p) {
		return (p == 2) || (p % 2 == 1);
	}

	/**
	 * Checks for primality of p. Intended to be used for checking the primality
	 * requirement of p in a Fp field. Not implemented yet, it is left up to the
	 * user to ensure the primality of p.
	 * @param p The number to check for primality
	 * @return For the time being, every number is considered a prime
	 */
	private static boolean isPrime(BigInteger p) {
		return true;
	}

}

/**
 * This class defines the abstract class for actual implementations of FieldP.
 * 
 * IMPORTANT: This class and its subclasses make use of the concept of immutability
 * of objects. If you make changes or subclass these classes, ensure immutability
 * or overwrite the methods affected.
 * 
 * @author Andreas Lochbihler
 */
abstract class FieldPAbstract extends FieldElement {
	/**
	 * Creates a new element of Fp which is the equivalence class of val.
	 * @param val A representant of the equivalence class to create. val does not
	 * have to be the smallest nonnegative representant.
	 * @return The equivalence class containing val in Fp.
	 */
	public abstract FieldPAbstract instance(long val);

	/**
	 * Tuning option. If you expect to need most of the inverses in Fp, i.e. want to
	 * divide by most of the elements, then this speeds the division up by computing
	 * the inverses ahead. If the implementation does not support computing inverses
	 * ahead of the point of time when they are needed, this method is supposed to
	 * do nothing.
	 */
	public abstract void computeAllInverses();

	/**
	 * Returns the element that is the equivalence class of this field containing
	 * the integral part of val as a representative.
	 * @param val Its integral part specifies the equivalence class to return.
	 * @return The equivalence class containing the integral part of val. 
	 */
	public FieldElement instance(double val) {
		return instance((long) val);
	}

}

/**
 * This class implements the operations in Fp where p &lt; PRIME_SEPARATION_BOUNDARY.
 * All computations can be done using long variables.
 * 
 * IMPORTANT: This class and its subclasses make use of the concept of immutability
 * of objects. If you make changes or subclass these classes, ensure immutability
 * or overwrite the methods affected.
 * 
 * @author Lochbihler Andreas
 */
abstract class FieldPLong extends FieldPAbstract {

	/** The least non-negative representant of the equivalence class */
	protected long value;

	/** The prime speicifying the number of elements in the field */
	protected long p;

	/**
	 * Returns a new element of the field Fp. The element is the
	 * equivalence class containing value.
	 * @param value The new element is the equivalence class of value
	 * @param p The number of elements in the Field. p must be prime.
	 *          It is up to the user to ensure that p^2<Long.MAX_VALUE otherwise
	 *          overflow problems may occur
	 */
	protected FieldPLong(long value, long p) {
		if (!FieldP.isPrime(p)) {
			throw new IllegalArgumentException(
				"p = "
					+ p
					+ " must be a prime in order to ensure the field property");
		}

		this.value = value;
		this.p = p;
		this.normalize();
	}

	/**
	 * Generates a new zero element of Fp. Only for internal use within this class.
	 * No parameter checking is done. Assumes that there is already at least one
	 * element of Fp in memory.
	 * @param p The number of elements in the field. Must be prime.
	 */
	protected FieldPLong(long p) {
		this.p = p;
	}

	/**
	 * Calculates the sum of this element and another one from the same
	 * field.
	 *
	 * @param val The other element to add
	 * @return The sum of both elements
	 * @throws IllegalArgumentException Thrown if you are trying to add elements from
	 *         different fields Fp.
	 */
	public FieldElement add(FieldElement val) throws IllegalArgumentException {
		FieldPLong op = (FieldPLong) val;
		if (op.p == this.p) {
			return instance(this.value + op.value);
		} else {
			throw new IllegalArgumentException(
				val
					+ " is from a different Fp than "
					+ this
					+ "! You cannot add them.");
		}
	}

	/**
	 * Calculates the product of this element and another one from the same
	 * field.
	 *
	 * @param val The other element to multiply
	 * @return The product of both elements
	 * @throws IllegalArgumentException Thrown if you are trying to multiply elements from
	 *         different fields Fp.
	 */
	public FieldElement multiply(FieldElement val)
		throws IllegalArgumentException {
		FieldPLong op = (FieldPLong) val;
		if (op.p == this.p) {
			return instance(this.value * op.value);
		} else {
			throw new IllegalArgumentException(
				val
					+ " is from a different Fp than "
					+ this
					+ "! You cannot multiply them.");
		}
	}

	/**
	 * Returns the zero element of the field Fp
	 * @return The zero element of the field Fp
	 */
	public FieldElement zero() {
		return instance(0);
	}

	/**
	 * Returns the one element of the field Fp
	 * @return The one element of the field Fp
	 */
	public FieldElement one() {
		return instance(1);
	}

	/**
	 * Computes the additive inverse
	 * @return The additive inverse
	 */
	public FieldElement negate() {
		return instance(-value);
	}

	/**
	 * Returns a string representation of the element. The string representation
	 * consists of the value of the smallest non-
	 * negative representant of the equivalence class in 10-adic, the letter m
	 * and the number of elements in the field in 10-adic.
	 * @return The string representation of the element.
	 */
	public String toString() {
		return value + "m" + p;
	}

	/**
	 * Compares this element with another element of the same field Fp.
	 * Note: This order does not respect addition or multiplication!
	 * @param o The element to compare to
	 * @return -1, if this is less, 0, if they are equal, 1, if this is bigger
	 */
	public int compareTo(Object o) {
		FieldPLong par = (FieldPLong) o;
		if (this.p == par.p) {
			long diff = this.value - par.value;
			return (diff > 0 ? 1 : (diff < 0 ? -1 : 0));
		} else {
			throw new IllegalArgumentException(
				o
					+ " is from a differend field than "
					+ this
					+ "! You cannot compare them");
		}
	}

	/**
	 * Normalizes the value of the element of Fp.
	 * This means that value will contain the least non-negative
	 * representative of the equivalence class in which value was at call time.
	 */
	public void normalize() {
		this.value = FieldPLong.normalize(this.value, this.p);
	}

	/**
	 * Normalizes val with respect to p.
	 * This means, it computes 0<=r<p such that p divides v-r
	 * @param val The value to normalize
	 * @param p The number of elements in Fp
	 * @return The normalized value of val in Fp.
	 */
	protected static long normalize(long val, long p) {
		val = val % p;
		if (val < 0) {
			val += p;
		}
		return val;
	}

	/**
	 * Computes the multiplicative inverse of val in Fp
	 * @param val The smallest non-negative representative of the equivalence class to invert in Fp.
	 * @return The multiplicative inverse of val in Fp, if it exists.
	 */
	protected long computeInverse(long val) {
		return FieldPLong.computeInverse(val, this.p);
	}

	/**
	 * Computes the inverse in Fp.
	 * @param val The value whose inverse is to be computed
	 * @param p The number of elements in the field.
	 * @return The inverse of val in Fp.
	 */
	protected static long computeInverse(long val, long p) {
		long a = p;
		long b = val;

		long w = 1;
		long x = 1;
		long y = 0;
		long z = 0;

		long r = 0;
		long q = 0;

		long new_z, new_w;

		// The invariant is x*p + y*value = a and z*p + w*value = b
		while (b != 0) {
			r = a % b;
			q = a / b;

			// swap variables
			a = b;
			b = r;

			new_z = x - q * z;
			new_w = y - q * w;
			x = z;
			y = w;
			z = new_z;
			w = new_w;
		}

		if (a != 1) {
			throw new InvalidOperationException(
				val + " is not invertible in F" + p);
		} else {
			// normalize y
			while (y < 0) {
				y += p;
			}
			return y;
		}
	}

	/**
	 * Maps a double value (0<=dval<1) to a element of FieldP
	 * @param dval: The random value to be mapped
	 * @return a random element corresponding to dval.
	 */
	public FieldElement randomValue(Random random) {
		return instance(p * random.nextDouble());
	}

}

/**
  * This class implements an element of Fp using a static lookup table for storing
  * multiplicative inverses. This lookup table for Fp is kept in memory as long as there is
  * at least one element of Fp in memory. If at some point no element of Fp can be
  * strongly referenced it is up to the garbage collector to remove the lookup table
  * for Fp.
  * 
  * IMPORTANT: This class and its subclasses make use of the concept of immutability
  * of objects. It is somewhat like a singleton except that there may be p objects of it, each
  * with a different attribute value.
  * If you make changes or subclass these classes, ensure immutability
  * or overwrite the methods affected, i.e. constructor, instance, enterInverses,
  * equals, clone.
  * 
  * @author Andreas Lochbihler
  */
class FieldPLongLookup extends FieldPLong {

	/**
	 * Maps from Long(p) objects to WeakReference objects referencing
	 * FieldPLongLookup[] arrays of length (p-1).
	 * The j-th entry contains the multiplicative inverse of j if already computed
	 * otherwise it is 0. The mapping has not to be right total.
	 */
	private static HashMap inverses = new HashMap();

	/**
	 * Reference to the lookup table of inverses for Fp.
	 * Must always be initialized. If the inverse of i is in the table and ist j,
	 * then there must also be inverseTable[j] = i and inverseTable[p-i]=p-j and
	 * inverseTable[p-j]=p-i.
	 * inverseTable[0] stores the zero element,
	 * inverseTable[1] stores the one element.
	 */
	private FieldPLongLookup[] inverseTable;

	/**
	 * Generates a new element of Fp. Only for internal use within this class.
	 * No parameter checking is done. 
	 * @param value The new element is the equivalence class of value
	 * @param p The number of elements in the field. Must be prime.
	 * @param inverseTable The table of inverses for this field Fp
	 */
	private FieldPLongLookup(
		long value,
		long p,
		FieldPLongLookup[] inverseTable) {
		super(p);
		this.value = value;
		this.inverseTable = inverseTable;
	}

	/**
	 * Computes the multiplicative inverse
	 * @return The multiplicative inverse
	 * @throws InvalidOperationException Thrown if there is no inverse
	 *         (i.e. this element is 0 or value is not relatively prime to p,)
	 */
	public FieldElement invert() {
		if (this.isZero()) {
			throw new InvalidOperationException("Inversion of 0");
		} else {
			/* The inverse has already been computed at creation time */
			return inverseTable[(int) value];
		}
	}

	/**
	 * Returns this field element since each element may exist only once!
	 * @return The field element
	 */
	public Object clone() {
		return this;
	}

	/**
	 * Check for equality of this with o. Two elements are equal if they represent the same
	 * equivalence class in the same field Fp. 
	 * @return True if and only if this is equal to o in the above sense.
	 */
	public boolean equals(Object o) {
		return (this == o);
	}

	/**
	 * Computes all the inverses for this field Fp and stores them in the
	 * lookup table.
	 */
	public void computeAllInverses() {
		if (p > 2) {
			// compute 2 separately
			if (inverseTable[2] == null) {
				long q = (p - 1) / 2;

				enterInverses(2, q);

				long a = 2;

				long q1 = q;
				while (q > 0 && a < q1) {
					if (q % 2 == 0) {
						q = q >> 1;
						a = a << 1;
					} else {
						q = (q + p) / 2; // p is odd since p is a prime > 2
						a = a << 1;
					}
					enterInverses(a, q);
				}
			}

			long a = 3;
			long q, a1;
			while (a < p) {
				if (inverseTable[(int) a] == null) {
					q = computeInverse(a);
					enterInverses(a, q);
					a1 = a;
					while (q % a1 == 0) {
						q /= a1;
						a = (a * a1) % p;
						enterInverses(a, q);
					}
				}
				a++;
			}

		}
	}

	/**
	 * Enters the multiplicative inverses of a, b, (p-a) and (p-b) in the lookup table. 
	 * @param a The inverse of b
	 * @param b The inverse of a
	 */
	private void enterInverses(long a, long b) {
		if (inverseTable[(int) a] == null) {
			enterInverses(a, b, p, inverseTable);
		}
	}

	/**
	 * Enters the multiplicative inverses of a, b, (p-a) and (p-b) in the lookup table.
	 * @param a The inverse of b wrt p
	 * @param b The inverse of a wrt p
	 * @param p The number of elements in the field.
	 * @param inverseTable The inverse lookup table.
	 */
	private static void enterInverses(
		long a,
		long b,
		long p,
		FieldPLongLookup[] inverseTable) {

		inverseTable[(int) a] = new FieldPLongLookup(b, p, inverseTable);
		if (a != b) {
			inverseTable[(int) b] = new FieldPLongLookup(a, p, inverseTable);
			if (inverseTable[(int) (p - a)] == null) {
				inverseTable[(int) (p - a)] =
					new FieldPLongLookup(p - b, p, inverseTable);
				inverseTable[(int) (p - b)] =
					new FieldPLongLookup(p - a, p, inverseTable);
			}
		} else if (inverseTable[(int) (p - b)] == null) {
			inverseTable[(int) (p - b)] =
				new FieldPLongLookup(p - a, p, inverseTable);
		}
	}

	/**
	 * Creates a new element of Fp which is the equivalence class of val.
	 * @param val A representant of the equivalence class to create. val does not
	 * have to be the smallest nonnegative representant.
	 * @return The equivalence class containing val in Fp.
	 */
	public FieldPAbstract instance(long val) {
		return instance(val, this.p, this.inverseTable);
	}

	/**
	 * Returns the element representing the equivalence class in which val is of the field Fp.
	 * If this is the first element of Fp to be created, the lookup table will be
	 * initialized. 
	 * @param val The value specifying the equivalence class to return
	 * @param p The number of elements in Fp
	 * @return The equivalence class containing val in Fp.
	 */
	public static FieldPAbstract instance(long val, long p) {
		/* Does the table of inverses exist? */
		Long pL = new Long(p);
		WeakReference wref = (WeakReference) inverses.get(pL);

		FieldPLongLookup[] inverseTable;

		if ((wref == null)
			|| ((inverseTable = (FieldPLongLookup[]) wref.get()) == null)) {
			// No -> create a new one
			inverseTable = new FieldPLongLookup[(int) p];

			/* add the zero element */
			inverseTable[0] = new FieldPLongLookup(0, p, inverseTable);

			// fill automatically inverses of 1 and (p-1)
			inverseTable[1] = new FieldPLongLookup(1, p, inverseTable);
			inverseTable[(int) p - 1] =
				new FieldPLongLookup(p - 1, p, inverseTable);

			// store for further usage
			inverses.put(pL, new WeakReference(inverseTable));
		}

		return instance(val, p, inverseTable);
	}

	/**
	 * Returns the element of Fp which val is in.
	 * @param val The value specifying the equivalence class to return
	 * @param p The number of elements of Fp
	 * @param inverseTable The lookup table for multiplicative inverses in Fp
	 * @return The equivalence class in which val is.
	 */
	private static FieldPAbstract instance(
		long val,
		long p,
		FieldPLongLookup[] inverseTable) {
		val = normalize(val, p);

		FieldPLongLookup inverseFP = inverseTable[(int) val];
		FieldPLongLookup element;
		if (inverseFP == null) {
			long inverse = computeInverse(val, p);
			enterInverses(val, inverse, p, inverseTable);
			element = inverseTable[(int) inverse];
		} else {
			element = inverseTable[(int) inverseFP.value];
		}
		return element;
	}

}

/**
 * This class implements an element of Fp where p is less than
 * FieldP.PRIME_SEPARATIN_BOUNDARY. Inverses are computed on demand and then
 * stored with the element. So if generating more than one object of the same
 * element of Fp then the computation of the multiplicative inverse may have
 * to be performed multiple times.
 * 
 * For small p it is recommended to use the lookup table version FieldPLongLookup. 
 * 
 * @author Andreas Lochbihler
 */
class FieldPLongNoLookup extends FieldPLong {

	/**
	 * Store the inverse of this element in Fp, if already computed, otherwise null.
	 */
	private FieldPLongNoLookup inverse;

	/**
	 * Create a new element of Fp being the equivalence class of value
	 * @param value A representant of the equivalence class to be created.
	 * @param p The number of elements in Fp. Must be prime.
	 */
	protected FieldPLongNoLookup(long value, long p) {
		super(value, p);
	}

	/**
	 * Generates a new zero element of Fp. Only for internal use within this class.
	 * No parameter checking is done. Assumes that there is already at least one
	 * element of Fp in memory.
	 * @param p The number of elements in the field. Must be prime.
	 */
	protected FieldPLongNoLookup(long p) {
		super(p);
	}

	/**
	 * Generates a new element of Fp being the equivalence class of value. Only
	 * for internal use within this class. No parameter checking is done.
	 * Assumes that there is already at least one element of Fp in memory.
	 * @param value The smallest nonnegative representant of the desired equivalence
	 *              class
	 * @param p The number of elements in the field. Must be prime.
	 * @param inverse The inverse of value in Fp. null if unknown.
	 */
	protected FieldPLongNoLookup(
		long value,
		long p,
		FieldPLongNoLookup inverse) {
		super(p);
		this.value = value;
		this.inverse = inverse;
	}

	/**
	 * Creates a new element of Fp which is the equivalence class of val.
	 * @param val A representant of the equivalence class to create. val does not
	 * have to be the smallest nonnegative representant.
	 * @return The equivalence class containing val in Fp.
	 */
	public FieldPAbstract instance(long val) {
		FieldPLongNoLookup res = new FieldPLongNoLookup(this.p);
		res.value = val;
		res.normalize();
		return res;
	}

	/**
	 * Computes the multiplicative inverse
	 * @return The multiplicative inverse
	 * @throws InvalidOperationException Thrown if there is no inverse
	 *         (i.e. this element is 0 or value is not relatively prime to p,)
	 */
	public FieldElement invert() {
		if (this.isZero()) {
			throw new InvalidOperationException("Inversion of 0");
		}

		if (inverse == null) {
			inverse = new FieldPLongNoLookup(computeInverse(value), p, this);
		}

		return inverse;
	}

	/**
	 * Supposed to compute all inverses. But since they are not stored in a
	 * lookup-table, do nothing.
	 */
	public void computeAllInverses() {
	}

}

/**
 * This class implements an element of Fp where p can be arbitrarily big by
 * using BigIntegers.
 * 
 * @author Andreas Lochbihler
 */
class FieldPBig extends FieldPAbstract {

	/**
	 * The smallest nonnegative representant of the equivalence class in Fp.
	 */
	private BigInteger value;

	/**
	 * If already computed, the multiplicative inverse of value. null otherwise
	 */
	private FieldPBig inverse;

	/**
	 * The number of elements in Fp. Must be a prime.
	 */
	private BigInteger p;

	/**
	 * Constructs a new element of Fp which is the equivalence class containing
	 * value.
	 * @param value Any representant of the desired equivalence class
	 * @param p The number of elements in Fp. Must be prime.
	 */
	FieldPBig(BigInteger value, BigInteger p) {
		this.value = value;
		this.p = p;
		normalize();
	}

	/**
	 * Creates a not-valid element of Fp (i.e. value is set to null). Only for
	 * internal use.
	 * @param p The p in Fp.
	 */
	private FieldPBig(BigInteger p) {
		this.p = p;
	}

	/**
	 * Creates a new element of Fp being the equivalence class of value and
	 * having the multiplicative inverse inverse. No parameter checking.
	 * Only for internal use.
	 * @param value The smallest nonnegative representant of the desired equivalence class 
	 * @param p The number of elements in Fp
	 * @param inverse The multiplicative inverse of value.
	 */
	private FieldPBig(BigInteger value, BigInteger p, FieldPBig inverse) {
		this.value = value;
		this.p = p;
		this.inverse = inverse;
	}

	/**
	 * Returns the sum of this and val.
	 * @param val The other operand.
	 * @return The sum of this and val.
	 */
	public FieldElement add(FieldElement val) {
		FieldPBig op = (FieldPBig) val;
		if (this.p.equals(op.p)) {
			return new FieldPBig(this.value.add(op.value), p);
		} else {
			throw new IllegalArgumentException(
				val
					+ " is from a different field Fp than "
					+ this
					+ "! You cannot add them.");
		}
	}

	/**
	 * Returns the product of this and val.
	 * @param val The other factor
	 * @return The product of this and val.
	 */
	public FieldElement multiply(FieldElement val) {
		FieldPBig op = (FieldPBig) val;
		if (this.p.equals(op.p)) {
			return new FieldPBig(this.value.multiply(op.value), p);
		} else {
			throw new IllegalArgumentException(
				val
					+ " is from a different field Fp than "
					+ this
					+ "! You cannot multiply them.");
		}
	}

	/**
	 * Returns the zero element of the field Fp
	 * @return The zero element of the field Fp
	 */
	public FieldElement zero() {
		FieldPBig res = new FieldPBig(p);
		res.value = BigInteger.ZERO;
		return res;
	}

	/**
	 * Returns the one element of the field Fp
	 * @return The one element of the field Fp
	 */
	public FieldElement one() {
		FieldPBig res = new FieldPBig(p);
		res.value = BigInteger.ONE;
		res.inverse = res;
		return res;
	}

	/**
	 * Computes the additive inverse
	 * @return The additive inverse
	 */
	public FieldElement negate() {
		return new FieldPBig(value.negate(), p);
	}

	/**
	 * Computes the multiplicative inverse
	 * @return The multiplicative inverse
	 * @throws InvalidOperationException Thrown if trying to inverst 0.
	 * @throws ArithmeticException Thrown if value is is not relatively prime to p.
	 */
	public FieldElement invert()
		throws InvalidOperationException, ArithmeticException {
		if (this.isZero()) {
			throw new InvalidOperationException("Multiplicative inversion of 0");
		}
		if (this.inverse == null) {
			this.inverse =
				new FieldPBig(this.value.modInverse(this.p), p, this);
		}
		return inverse;
	}

	/**
	 * Compares this element with another element of the same field Fp.
	 * Note: This order does not respect addition or multiplication!
	 * @param o The element to compare to
	 * @return -1, if this is less, 0, if they are equal, 1, if this is bigger
	 */
	public int compareTo(Object o) {
		FieldPBig par = (FieldPBig) o;
		if (this.p.equals(par.p)) {
			return value.compareTo(par.value);
		} else {
			throw new IllegalArgumentException(
				o
					+ " is from a differend field than "
					+ this
					+ "! You cannot compare them");
		}
	}

	/**
	 * Creates a new element of Fp which is the equivalence class of val.
	 * @param val A representant of the equivalence class to create. val does not
	 * have to be the smallest nonnegative representant.
	 * @return The equivalence class containing val in Fp.
	 */
	public FieldPAbstract instance(long value) {
		return new FieldPBig(BigInteger.valueOf(value));
	}

	/**
	 * Computation of all inverses is not supported for FieldPBig. Since this is
	 * only a tuning option, it is transparent to the user.
	 */
	public void computeAllInverses() {
	}

	/**
	 * Normalizes the value of the element of Fp.
	 * This means that value will contain the least non-negative
	 * representative of the equivalence class value was in at call time.
	 */
	public void normalize() {
		this.value = this.value.mod(this.p);
	}

	/**
	 * Returns a pseudo-random element. All elements are approximately equally probable.
	 * @return a pseudo-random element
	 */
	public FieldElement randomValue() {
		return new FieldPBig(
			new BigDecimal(this.p)
				.multiply(new BigDecimal(Math.random()))
				.toBigInteger(),
			this.p);
	}

	/**
	 * Maps a double value (0<=dval<1) to a element of FieldP
	 * @param dval: The random value to be mapped
	 * @return a random element corresponding to dval.
	 */
	public FieldElement randomValue(Random random) {
		return new FieldPBig(
			new BigDecimal(this.p)
				.multiply(new BigDecimal(random.nextDouble()))
				.toBigInteger(),
			this.p);
	}

}
