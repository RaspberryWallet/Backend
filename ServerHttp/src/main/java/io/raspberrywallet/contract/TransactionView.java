package io.raspberrywallet.contract;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@Getter
@Setter
public class TransactionView {
    @NonNull
    private String txHash;
    @NonNull
    private long creationTimestamp;
    @Nullable
    private String fromAddress;
    @Nullable
    private String toAddress;
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
            "\nfromAddress: " + fromAddress +
            "\ntoAddress: " + toAddress +
            "\namountFromMe: " + amountFromMe +
            "\namountToMe: " + amountToMe +
            "\nfee: " + fee +
            "\nconfirmations: " + confirmations;
    }
}
