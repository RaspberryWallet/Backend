package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// performs reductions (sum, max, min) on Matrix and Vector objects
abstract class Reduction {

	public FieldElement reducedValue;

	public void init(FieldElement firstValue) {
		reducedValue = firstValue;
	}

	public abstract void track(FieldElement currValue);
}
