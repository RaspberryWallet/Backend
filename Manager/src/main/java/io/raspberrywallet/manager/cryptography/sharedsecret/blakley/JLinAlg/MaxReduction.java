package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// computes maximum over elements of Vector or Matrix
class MaxReduction extends Reduction {

	public void init(FieldElement firstValue) {
		reducedValue = firstValue.zero();
	}

	public void track(FieldElement currValue) {
		if (currValue.gt(reducedValue)) {
			reducedValue = currValue;
		}
	}
}
