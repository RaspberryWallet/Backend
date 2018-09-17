package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// difference of two FieldElements
class MultiplyOperator implements DyadicOperator {

	public FieldElement apply(FieldElement x, FieldElement y) {
		return x.multiply(y);
	}

}
