package io.raspberrywallet.contract;

import lombok.Getter;
import lombok.NonNull;

import java.util.Date;
import java.util.List;

@Getter
public class TransactionView {
    @NonNull
    private String txHash;
    @NonNull
    private long creationTimestamp;
    @NonNull
    private List<String> inputAddresses;
    @NonNull
    private List<String> outputAddresses;
    @NonNull
    private String amountFromMe;
    @NonNull
    private String amountToMe;
    @NonNull
    private String fee;
    private int confirmations;

    public TransactionView(String txHash, long creationTimestamp, List<String> inputAddresses,
                           List<String> outputAddresses, String amountFromMe, String amountToMe,
                           String fee, int confirmations) {
        this.txHash = txHash;
        this.creationTimestamp = creationTimestamp;
        this.inputAddresses = inputAddresses;
        this.outputAddresses = outputAddresses;
        this.amountFromMe = amountFromMe;
        this.amountToMe = amountToMe;
        this.fee = fee;
        this.confirmations = confirmations;
    }

    @Override
    public String toString() {
        return "txHash: " + txHash +
            "\ncreationDate: " + new Date(creationTimestamp).toString() +
            inputAddresses.stream().reduce("\ninputAddresses: ", (acc, address) -> acc + "\n\t" + address) +
            outputAddresses.stream().reduce("\noutputAddresses: ", (acc, address) -> acc + "\n\t" + address) +
            "\namountFromMe: " + amountFromMe +
            "\namountToMe: " + amountToMe +
            "\nfee: " + fee +
            "\nconfirmations: " + confirmations;
    }
}
