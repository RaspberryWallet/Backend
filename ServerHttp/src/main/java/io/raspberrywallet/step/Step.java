package io.raspberrywallet.step;

public abstract class Step {
    private final String instruction;

    public Step(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

}
