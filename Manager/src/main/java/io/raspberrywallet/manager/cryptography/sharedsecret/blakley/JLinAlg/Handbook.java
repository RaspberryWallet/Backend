/*
  This file conains Java versions of the algorithms HQR, ELMHES, and BALANCE,
  as presented in 
 
  @Book{LinearAlgebraHandbook,
    author = 	 {J.H. Wilkinson and C. Reinsch},
    title = 	 {Handbook for Automatic Computation},
    publisher =	 {Springer-Verlag},
    address =    {New York}
    year = 	 {1971},
    volume = 	 {II: Linear Algebra}
  }

  Author: Simon D. Levy

*/

package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

class Handbook {

	// Finds the eigenvalues of a real upper Hessenberg matrix, H,
	// stored in the array h[0:n-1][0:n-1], and stores the real parts
	// in the array wr[0:n-1] and the imaginary parts in the array
	// wi[0:n-1].  macheps is the relative machine precision. The
	// procedure fails if any eigenvalue takes more than 30
	// iterations.
	//
	// Additional parameters:
	//
	//   cnt - gets number of iterations for each eigenvalue (negative
	//         when two roots are found at once)
	//
	//   macheps - machine precision   
	//
	
	/**
	 * Returns a complex vector containing the eigenvalues of a given matrix.
	 * Eigenvalues are computed as follows:<br><br>
	 *   (1) Balance the matrix <br>
	 *   (2) Reduce the balanced matrix to Hessenberg form <br>
	 *   (3) Run the QR algorithm on the Hessenberg matrix to obtain the 
	 *       eigenvalues.<br><br>
	 * The algorithms used for steps 1-3 are adapted from
	 *<PRE>&#064Book{LinearAlgebraHandbook,
	 *      author = {J.H. Wilkinson and C. Reinsch},
	 *      title =  {Handbook for Automatic Computation},
	 *      publisher = {Springer-Verlag},
	 *      address = {New York}
	 *      year = {1971},
	 *      volume = {II: Linear Algebra}
	 * }
	 * </PRE>
	 *
	 * @param a the matrix
	 * @return Vector of Complex
	 * @throws InvalidOperationException if theMatrix does not contain doubles
	 * @throws InvalidOperationException if theMatrix is not square
	 */
	public static Vector eig(Matrix a) throws InvalidOperationException {
		double[][] vals = null;
		try {
			vals = doubleValues(a);
		} catch (Exception e) {
			throw new InvalidOperationException("Matrix must contain only DoubleWrappers");
		}

		int m = a.getRows(), n = a.getCols();

		if (m != n) {
			throw new InvalidOperationException("Matrix must be square");
		}

		double[] wr = new double[n];
		double[] wi = new double[n];

		int[] iint = new int[n];
		int[] cnt = new int[n];
		int[] lohi = new int[2];
		double[] d = new double[n];

		int radix = 2;
		double macheps = 1e-20;

		Handbook.balance(vals, lohi, d, n, radix);

		Handbook.elmhes(vals, iint, n, lohi[0], lohi[1]);

		Handbook.hqr(vals, wr, wi, cnt, n, macheps);

		Complex[] entries = new Complex[n];

		for (int i = 0; i < n; ++i) {
			entries[i] = new Complex(wr[i], wi[i]);
		}

		return new Vector(entries);
	}
	
	protected static void hqr(
		double[][] h,
		double[] wr,
		double[] wi,
		int[] cnt,
		int n,
		double macheps) {

		double t = 0;

		while (n > 0) { // nextw 

			int its = 0, na = n - 1;

			while (true) { // nextit

				int l, m;

				// look for single small sub-diagonal element
				boolean found = false;
				for (l = n; l >= 2; l--) {
					if (abs(get(h, l, l - 1))
						<= macheps
							* (abs(get(h, l - 1, l - 1)) + abs(get(h, l, l)))) {
						found = true;
						break; // goto cont1
					}
				}

				if (!found)
					l = 1;

				// cont1

				double x = get(h, n, n);

				if (l == n) { // goto onew (one root found)
					set(wr, n, x + t);
					set(wi, n, 0);
					n = na;
					break; // goto nextw
				}

				double y = get(h, na, na), w = get(h, n, na) * get(h, na, n);

				if (l == na) { // goto twow (two roots found)
					double p = (y - x) / 2, q = p * p + w;
					y = sqrt(abs(q));
					set(cnt, n, -its);
					set(cnt, na, its);
					x += t;
					if (q > 0) { // real pair
						if (p < 0)
							y = -y;
						y = p + y;
						set(wr, na, x + y);
						set(wr, n, x - w / y);
						set(wi, na, 0);
						set(wi, n, 0);
					} else { // complex pair
						set(wr, na, x + p);
						set(wr, n, x + p);
						set(wi, na, y);
						set(wi, n, -y);
					}
					n -= 2;
					break; // goto nextw
				}

				if (its == 30) {
					System.err.println("HQR failed after 30 iterations");
					return; // goto fail
				}

				if (its == 10 || its == 20) { // form exceptional shift
					t += x;
					for (int i = 1; i <= n; ++i)
						set(h, i, i, get(h, i, i) - x);
					double s = abs(get(h, n, na)) + abs(get(h, na, n - 2));
					x = y = 0.75 * s;
					w = -0.4375 * s * s;
				}
				its++;

				// look for two consecutive sub-diagonal elements
				double p = 0, q = 0, r = 0, s = 0, z = 0;
				for (m = n - 2; m >= l; m--) {
					z = get(h, m, m);
					r = x - z;
					s = y - z;
					p = (r * s - w) / get(h, m + 1, m) + get(h, m, m + 1);
					q = get(h, m + 1, m + 1) - z - r - s;
					r = get(h, m + 2, m + 1);
					s = abs(p) + abs(q) + abs(r);
					p = p / s;
					q = q / s;
					r = r / s;
					if (m == l)
						break;
					if (abs(get(h, m, m - 1)) * (abs(q) + abs(r))
						<= macheps
							* abs(p)
							* (abs(get(h, m - 1, m - 1))
								+ abs(z)
								+ abs(get(h, m + 1, m + 1))))
						break; // goto cont2
				}

				// cont2

				for (int i = m + 2; i <= n; ++i)
					set(h, i, i - 2, 0);
				for (int i = m + 3; i <= n; ++i)
					set(h, i, i - 3, 0);

				// double QR step involving rows l to n and columns m to n
				for (int k = m; k <= na; ++k) {
					boolean notlast = (k != na);
					if (k != m) {
						p = get(h, k, k - 1);
						q = get(h, k + 1, k - 1);
						r = notlast ? get(h, k + 2, k - 1) : 0;
						x = abs(p) + abs(q) + abs(r);
						if (x == 0)
							break; // goto cont3
						p /= x;
						q /= x;
						r /= x;
					}
					s = sqrt(p * p + q * q + r * r);
					if (p < 0)
						s = -s;
					if (k != m)
						set(h, k, k - 1, -s * x);
					else if (l != m)
						set(h, k, k - 1, -get(h, k, k - 1));
					p += s;
					x = p / s;
					y = q / s;
					z = r / s;
					q /= p;
					r /= p;

					// row modification
					for (int j = k; j <= n; ++j) {
						p = get(h, k, j) + q * get(h, k + 1, j);
						if (notlast) {
							p = p + r * get(h, k + 2, j);
							set(h, k + 2, j, get(h, k + 2, j) - p * z);
						}
						set(h, k + 1, j, get(h, k + 1, j) - p * y);
						set(h, k, j, get(h, k, j) - p * x);
					}
					int j = (k + 3) < n ? k + 3 : n;

					// column modification
					for (int i = l; i <= j; ++i) {
						p = x * get(h, i, k) + y * get(h, i, k + 1);
						if (notlast) {
							p = p + z * get(h, i, k + 2);
							set(h, i, k + 2, get(h, i, k + 2) - p * r);
						}
						set(h, i, k + 1, get(h, i, k + 1) - p * q);
						set(h, i, k, get(h, i, k) - p);
					}

				} // k = m..na

				// cont3

			} // goto nextit            
		}
	}

	// Given the unsymmetric matrix A, stored in the array
	// a[0:n-1,0:n-1], this procedure reduces the sub-matrix of order
	// l-k+1, which starts at the element a[k-1][k-1], and finishes at
	// the element a[l-1][l-1], to Hessenberg form H, by
	// non-orthogonal elementary transformations.  The matrix H is
	// overwritten on A with details of the transformations stored in
	// the remaining triangle under H and in the array iint[k-1:l-1].
	//
	// Additional parameters:
	//
	//   iint - gets row and column interchanges involved in reduction
	//
	protected static void elmhes(
		double[][] a,
		int[] iint,
		int n,
		int k,
		int l) {

		int la = l - 1;

		for (int m = k + 1; m <= la; ++m) {
			int i = m;
			double x = 0;
			for (int j = m; j <= l; ++j) {
				if (abs(get(a, j, m - 1)) > abs(x)) {
					x = get(a, j, m - 1);
					i = j;
				}
			}
			set(iint, m, i - 1);
			if (i != m) { // interchange rows and columns of A
				for (int j = m - 1; j <= n; ++j) {
					swap(a, i, j, m, j);
				}
				for (int j = 1; j <= l; ++j) {
					swap(a, j, i, j, m);
				}
			} // interchange
			if (x != 0) {
				for (i = m + 1; i <= l; ++i) {
					double y = get(a, i, m - 1);
					if (y != 0) {
						set(a, i, m - 1, y / x);
						y = get(a, i, m - 1);
						for (int j = m; j <= n; ++j) {
							set(a, i, j, get(a, i, j) - y * get(a, m, j));
						}
						for (int j = 1; j <= l; ++j) {
							set(a, j, m, get(a, j, m) + y * get(a, j, i));
						}
					}
				} // i
			}
		} // m
	}

	// reduce the norm of a[0:n-1][0:n-1] by exact diagonal similarity
	// transformations stored in d[0:n-1].  Modified from original:
	// check for scaled identity matrix (k==0) in L1, L2 phases.
	//
	// Additional parameters:
	//
	//   lohi - two integers such that a[i][j] is equal to zero if
	//            (1) i > j and
	//            (2) j=0,...,lo-2 or i=hi,...,n-1
	//
	protected static void balance(
		double[][] a,
		int[] lohi,
		double[] d,
		int n,
		int b) {

		int b2 = b * b, l = 1, k = n;

		// search for rows isolating an eigenvalue and push them down
		for (boolean found = true; found && (k > 0);) { // L1
			for (int j = k; j >= 1; --j) {
				double r = 0;
				for (int i = 1; i <= k; ++i) {
					if (i != j) {
						r += abs(get(a, j, i));
					}
				}
				if (r == 0) {
					exc(k, a, d, n, j, k, l);
					k--;
				} // goto L1
				else {
					found = false;
				}
			}
		}

		// search for columns isolating an eigenvalue and push them left
		for (boolean found = true; found && (k > 0);) { // L2
			for (int j = l; j <= k; ++j) {
				double c = 0;
				for (int i = l; i <= k; ++i) {
					if (i != j) {
						c += abs(get(a, i, j));
					}
				}
				if (c == 0) {
					exc(l, a, d, n, j, k, l);
					l++;
				} // goto L2
				else {
					found = false;
				}
			}
		}

		// now balance the submatrix in rows l through k
		lohi[0] = l;
		lohi[1] = k;
		for (int i = l; i <= k; ++i)
			set(d, i, 1);
		while (true) { // iteration
			boolean noconv = false;
			for (int i = l; i <= k; ++i) {
				double c = 0, r = 0;
				for (int j = l; j <= k; ++j) {
					if (j != i) {
						c += abs(get(a, j, i));
						r += abs(get(a, i, j));
					}
				}
				double g = r / b, f = 1, s = c + r;
				while (c < g) { // L3
					f *= b;
					c *= b2;
				}
				g = r * b;
				while (c >= g) { // L4
					f /= b;
					c /= b2;
				}
				// now balance
				if ((c + r) / f < 0.95 * s) {
					g = 1 / f;
					set(d, i, get(d, i) * f);
					noconv = true;
					for (int j = l; j <= n; ++j)
						set(a, i, j, get(a, i, j) * g);
					for (int j = l; j <= k; ++j)
						set(a, j, i, get(a, j, i) * f);
				}
			}
			if (!noconv)
				break;
		}
	}

	// exchange routine used by BALANCE
	private static void exc(
		int m,
		double[][] a,
		double[] d,
		int n,
		int j,
		int k,
		int l) {
		set(d, m, j);

		if (j != m) {
			for (int i = 1; i <= k; ++i) {
				swap(a, i, j, i, m);
			}
			for (int i = l; i <= n; ++i) {
				swap(a, j, i, m, i);
			}
		}
	}

	// get, set allow us to use indices 1..N, as in original code -------------

	private static double get(double[][] a, int i, int j) {
		return a[i - 1][j - 1];
	}

	private static void set(double[] a, int i, double v) {
		a[i - 1] = v;
	}

	private static double get(double[] a, int i) {
		return a[i - 1];
	}

	private static void set(double[][] a, int i, int j, double v) {
		a[i - 1][j - 1] = v;
	}

	private static void set(int[] a, int i, int v) {
		a[i - 1] = v;
	}

	// additional macros ------------------------------------------------------

	private static double abs(double x) {
		return Math.abs(x);
	}

	private static double sqrt(double x) {
		return Math.sqrt(x);
	}

	private static void swap(double[][] a, int r, int c, int rr, int cc) {
		double y = get(a, r, c);
		set(a, r, c, get(a, rr, cc));
		set(a, rr, cc, y);
	}
	
	/**
	 * Returns an array of double-precision floating-point values
	 * contained in a Matrix.
	 * @param theMatrix
	 * @return 2D array of double
	 * @throws InvalidOperationException if theMatrix does not contain doubles
	 */
	public static double[][] doubleValues(Matrix theMatrix)
		throws InvalidOperationException {
		check_double(theMatrix);
		int m = theMatrix.getRows(), n = theMatrix.getCols();
		double[][] result = new double[m][n];
		for (int i = 1; i <= m; ++i) {
			for (int j = 1; j <= n; ++j) {
				result[i - 1][j - 1] = doubleValue(theMatrix, i, j);
			}
		}
		return result;
	}
	
	/**
	 * Returns a 2xN array containing the real and imaginary components of
	 * a complex vector of length N.
	 * @param theVector containing the Complex values
	 * @return 2D array of double
	 * @throws InvalidOperationException if theVector does not contain Complex
	 */
	public static double[][] complexValues(Vector theVector)
		throws InvalidOperationException {

		check_complex(theVector);

		int n = theVector.length();

		double[][] values = new double[2][n];

		for (int i = 0; i < n; ++i) {
			FieldElement f = theVector.entries[i];
			Complex c = (Complex) f;
			values[0][i] = c.getReal().doubleValue();
			values[1][i] = c.getImaginary().doubleValue();
		}

		return values;
	}
	
	/**
	 * Returns the double-precision floating-point value in a FieldElement.
	 * @param d the element
	 * @return double contained in d
	 * @throws InvalidOperationException if d does not contain a double
	 */
	public static double doubleValue(FieldElement d)
		throws InvalidOperationException {
		check_double(d);
		return unwrap(d);
	}
	
	/**
	 * Returns the Ith double-precision floating-point value in a Vector.
	 * First index is 1.
	 * @param x the Vector
	 * @param i the index
	 * @return double contained in at v(i)
	 * @throws InvalidOperationException if v does not contain double
	 * @throws InvalidOperationException if the index is out of bounds
	 */
	public static double doubleValue(Vector x, int i)
		throws InvalidOperationException {
		check_double(x);
		return unwrap(x.getEntry(i));
	}

	/**
	 * Returns the I,Jth double-precision floating-point value in a Matrix.
	 * First index is 1,1.
	 * @param a the Matrix
	 * @param i row index
	 * @param j column index
	 * @return double contained in at a(i,j)
	 * @throws InvalidOperationException if v does not contain double
	 * @throws InvalidOperationException if either index is out of bounds
	 */
	public static double doubleValue(Matrix a, int i, int j)
		throws InvalidOperationException {
		check_double(a);
		return unwrap(a.get(i, j));
	}
	
	
	private static double unwrap(FieldElement f) {
		return ((DoubleWrapper) f).value;
	}

	// Double, double, toil and trouble / If this don't work, your
	// code is rubble

	private static void check_double(Matrix a)
		throws InvalidOperationException {
		check_double(a.get(1, 1));
	}


	private static void check_double(FieldElement value)
		throws InvalidOperationException {

		check_type(value, new DoubleWrapper(0), "double");
	}

	private static void check_double(Vector v)
		throws InvalidOperationException {
		check_double(v.getEntry(1));
	}
	
	private static void check_complex(Vector v)
		throws InvalidOperationException {
		check_complex(v.getEntry(1));
	}
	
	private static void check_complex(FieldElement value)
		throws InvalidOperationException {

		check_type(value, new Complex(0, 0), "complex");
	}
	
	private static void check_complex(Matrix a)
		throws InvalidOperationException {
		check_complex(a.get(1, 1));
	}
	
	private static void check_type(
		FieldElement value,
		FieldElement check,
		String name)
		throws InvalidOperationException {

		if (!value.getClass().equals(check.getClass())) {
			String err = "Matrix or Vector doesn't contain " + name;
			throw new InvalidOperationException(err);
		}
	}
}
