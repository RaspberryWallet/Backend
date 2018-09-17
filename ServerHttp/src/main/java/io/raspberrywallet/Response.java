package io.raspberrywallet;

import io.raspberrywallet.step.Step;
import org.jetbrains.annotations.Nullable;

public class Response {
    @Nullable
    private final Step nextStep;
    private final Status status;

    public Response(@Nullable Step nextStep, Status status) {
        this.nextStep = nextStep;
        this.status = status;
    }

    public enum Status {
        FAILED, OK;

        public String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @Nullable
    public Step getNextStep() {
        return nextStep;
    }


    public Status getStatus() {
        return status;
    }

}
