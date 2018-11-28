package io.raspberrywallet.contract;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TransactionView {
    @NonNull
    private String txHash;
    @NonNull
    private long creationTimestamp;
    @NonNull
    private List<String> inputAddresses = new ArrayList<>();
    @NonNull
    private List<String> outputAddresses = new ArrayList<>();
    @NonNull
    private String amountFromMe;
    @NonNull
    private String amountToMe;
    @NonNull
    private String fee;
    private int confirmations;

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
