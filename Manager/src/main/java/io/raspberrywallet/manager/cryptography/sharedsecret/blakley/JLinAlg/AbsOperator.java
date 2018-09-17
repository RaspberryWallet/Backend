package io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg;

// absolute value computation for FieldElement
class AbsOperator implements MonadicOperator {

    public FieldElement apply(FieldElement x) {
	return x.lt(x.zero()) ? x.zero().subtract(x) : x;
    }

}
