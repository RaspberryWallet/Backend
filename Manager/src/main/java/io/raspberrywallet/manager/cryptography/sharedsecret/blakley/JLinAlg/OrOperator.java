package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// logical OR of two FieldElements
class OrOperator implements DyadicOperator {

	public FieldElement apply(FieldElement x, FieldElement y) {
		return (x.isZero() && y.isZero()) ? x.zero() : x.one();
	}
}
