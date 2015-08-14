package utils;

/**
 * Stores the state of a byte : whether it was deleted, inserted, or equal
 */
public class ByteDiff {

    public ByteDiff(DiffUtils.Operation operation, String byteHex) {
        this.operation = operation;
        this.byteHex = byteHex;
    }

    /**
     * The state of the byte
     */
    private DiffUtils.Operation operation;

    public DiffUtils.Operation getOperation() {
        return operation;
    }

    public void setOperation(DiffUtils.Operation operation) {
        this.operation = operation;
    }

    /**
     * The byte that was read
     */
    String byteHex;

    public String getByteHex() {
        return byteHex;
    }

    public void setByteHex(String byteHex) {
        this.byteHex = byteHex;
    }

    @Override
    public String toString(){
        return "Byte: " + byteHex + " - Operation: " + operation;
    }
}
