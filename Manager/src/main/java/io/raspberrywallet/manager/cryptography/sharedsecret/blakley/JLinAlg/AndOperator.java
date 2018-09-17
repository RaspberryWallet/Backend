package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// logical AND of two FieldElements
class AndOperator implements DyadicOperator {

	public FieldElement apply(FieldElement x, FieldElement y) {
		return (x.isZero() || y.isZero()) ? x.zero() : x.one();
	}
}
