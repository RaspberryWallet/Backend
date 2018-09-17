package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

import java.io.Serializable;

/**
 * This class provides a run-time exception that gets thrown whenever
 * an invalid operation is attempted on a Vector or Matrix.
 */

public class InvalidOperationException extends RuntimeException implements Serializable{

	public InvalidOperationException(String theMessage) {
		super(theMessage);
	}
}
