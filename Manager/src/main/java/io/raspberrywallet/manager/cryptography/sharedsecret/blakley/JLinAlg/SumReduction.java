package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// computes sum over elements of Vector or Matrix
class SumReduction extends Reduction {

	public void track(FieldElement currValue) {
		reducedValue = reducedValue.add(currValue);
	}
}
