package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// class to return FieldElement.one() or FieldElement.zero(), depending on
// result of FieldElement not-equal-to comparison
class NotEqualToComparator extends FEComparator {

	public boolean compare(FieldElement a, FieldElement b) {
		return !a.equals(b);
	}
}
