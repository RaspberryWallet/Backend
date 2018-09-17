package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// abstract class to return FieldElement.one() or FieldElement.zero(),
// depending on result of comparison
abstract class FEComparator {

	public abstract boolean compare(FieldElement a, FieldElement b);
}
