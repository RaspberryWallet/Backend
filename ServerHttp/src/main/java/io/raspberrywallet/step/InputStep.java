package io.raspberrywallet.step;

public final class InputStep extends Step {
    private byte[] inputData;

    public InputStep(String instruction, byte[] inputData) {
        super(instruction);
        this.inputData = inputData;
    }


    public byte[] getInputData() {
        return inputData;
    }

    public void setInputData(byte[] inputData) {
        this.inputData = inputData;
    }
}
