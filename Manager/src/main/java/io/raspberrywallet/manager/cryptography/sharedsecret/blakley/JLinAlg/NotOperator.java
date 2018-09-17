package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// logical negation of FieldElement
class NotOperator implements MonadicOperator {

	public FieldElement apply(FieldElement x) {
		return x.isZero() ? x.one() : x.zero();
	}
}
