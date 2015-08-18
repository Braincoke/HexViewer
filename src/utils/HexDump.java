package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generate a hexdump of a file
 */
public class HexDump {

    public static final int BYTES_PER_LINE = 16;
    public static final int ERROR_OFFSET = 1028;

    /**
     * Reads a file and outputs a list of formatted lines held in HexDumpLine objects
     * @param file      The file to read
     * @param offset    The starting point of the hex dump
     * @param nbLines   The number of lines to read
     * @return          The list of formatted lines
     */
    public static List<HexDumpLine> getLines(File file, long offset, int nbLines) {
        ArrayList<HexDumpLine> hexDumpLines = new ArrayList<>();
        int remainingLines = nbLines;
        long currentOffset = offset;
        try {
            InputStream is = new FileInputStream(file);
            long skipped = is.skip(offset);
            if(skipped!=offset){
                currentOffset = skipped;
            }
            while(is.available()>0 && remainingLines>0){
                //Read 16 bytes to create a hex dump line
                hexDumpLines.add(new HexDumpLine(is, currentOffset));
                remainingLines--;
                currentOffset += BYTES_PER_LINE;
            }
            is.close();
        } catch (IOException e){
            int size = hexDumpLines.size();
            for(int i=size; i<nbLines; i++) {
                hexDumpLines.add(new HexDumpLine(ERROR_OFFSET, "..IO.EXCEPTION..", "..IO.EXCEPTION.."));
            }
            Logger.getLogger(HexDump.class.getName()).log(Level.WARNING, "Error when loading the file : " + file.getPath(), e);
        }
        return hexDumpLines;
    }

    /**
     * Reads a part of a file and outputs the formatted hex dump in a String
     * @param file      The file to read
     * @param offset    The byte from where to start reading (starts at 0)
     * @param lines     The number of lines to display. A line is composed of 32 hexadecimal chars (16 bytes)
     * @return          The formatted hex dump
     * @throws IOException
     */
    public static String getString(File file, long offset, int lines) throws IOException {
        String out = "";
        InputStream is = new FileInputStream(file);
        long skipped = is.skip(offset);
        int remaining = lines;

        int lineOffset = lines;
        while (is.available()>0 && remaining>0){
            out += formatLineOutput(is, lineOffset);
            lineOffset++;
            remaining--;
        }
        is.close();
        return out;
    }

    /**
     * Reads an entire file an output the formatted hex dump in a String
     * @param file      The file to read
     * @return          The formatted hex dump ( [offset]  [hex dump]  [strings] )
     * @throws IOException
     */
    public static String getString(File file) throws IOException {
        String out = "";
        InputStream is = new FileInputStream(file);
        int i = 0;

        while (is.available() > 0) {
            out += formatLineOutput(is, i);
            i++;
        }
        is.close();
        return out;
    }

    /**
     * Reads 16 bytes of a file and format the output to show the hex dump in the following way :
     * [offset]    [hex dump]    [strings]
     * @param is            The input stream from where to read
     * @param lineOffset    The beginning of the line
     * @return              The formatted line
     * @throws IOException
     */
    public static String formatLineOutput(InputStream is, long lineOffset) throws IOException {
        String out = "";
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder("   ");
        out += String.format("%04X  ", lineOffset * 16);
        for (int j = 0; j < 16; j++) {
            if (j==8){
                sb1.append(" ");
            }
            if (is.available() > 0) {
                int value = is.read();
                sb1.append(String.format("%02X ", value));
                if (!Character.isISOControl(value)) {
                    sb2.append((char)value);
                }
                else {
                    sb2.append(".");
                }
            }
            else {
                for (;j < 16;j++) {
                    sb1.append("   ");
                }
            }
        }
        out += sb1;
        out += sb2 + "\n";
        return out;
    }

    /**
     * Reads byteNumber bytes of a file and outputs the hex dump of the file
     * Note that only the conversion 1 byte -> 2 hex chars is done.
     * @param file          The file to read
     * @param offset        The offset from where to start reading
     * @param byteNumber    The number of bytes to read
     * @return              The hex dump of the bytes read
     */
    public static String getHexDump(File file, long offset, long byteNumber) throws IOException {
        StringBuilder hexDump = new StringBuilder();
        InputStream is = new FileInputStream(file);
        long skipped = is.skip(offset);
        long remaining = byteNumber;

        while (is.available()>0 && remaining>0){
            int value = is.read();
            hexDump.append(String.format("%02X", value));
            remaining--;
        }
        is.close();
        return hexDump.toString();
    }
}
