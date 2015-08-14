package utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Representation of a hexdump line.
 * A line is composed of an offset and 16 hexadecimal characters (32 bytes).
 */
public class HexDumpLine {

    public HexDumpLine(int offset, String hex, String strings){
        this.offset = offset;
        this.hex = hex;
        this.strings = strings;
    }

    /**
     * Build a hex dump line from an input stream
     * Reads 32 bytes from the input stream in order to create the line
     * @param is        The input stream
     * @param offset    The offset of the line used only to display it later
     */
    public HexDumpLine(InputStream is, long offset){
        this.offset = offset;
        StringBuilder lineHex = new StringBuilder();
        StringBuilder lineStrings = new StringBuilder();
        try {
            for (int j = 0; j < 16; j++) {
                if (is.available() > 0) {
                    int value = is.read();
                    lineHex.append(String.format("%02X", value));
                    if (!Character.isISOControl(value)) {
                        lineStrings.append((char) value);
                    } else {
                        lineStrings.append(".");
                    }
                } else {
                    for (; j < 16; j++) {
                        lineHex.append("..");
                        lineStrings.append(".");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.hex = lineHex.toString();
        this.strings = lineStrings.toString();
    }

    /**
     * The byte offset of the line
     * Determined by the offset of the first byte of the line.
     */
    private long offset;

    public long getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * The hexadecimal line
     */
    private String hex;

    public String getHex() {
        return hex;
    }

    public char[] getHexChars(){
        return hex.toCharArray();
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    /**
     * The strings in the hexadecimal line
     */
    private String strings;

    public String getStrings() {
        return strings;
    }

    public void setStrings(String strings) {
        this.strings = strings;
    }


}
