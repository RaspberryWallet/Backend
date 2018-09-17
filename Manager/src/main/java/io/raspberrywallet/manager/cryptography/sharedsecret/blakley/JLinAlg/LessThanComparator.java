package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// class to return FieldElement.one() or FieldElement.zero(), depending on
// result of FieldElement less-than comparison
class LessThanComparator extends FEComparator {

	public boolean compare(FieldElement a, FieldElement b) {
		return a.lt(b);
	}
}
