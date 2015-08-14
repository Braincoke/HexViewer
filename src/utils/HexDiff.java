package utils;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Generates a hex diff of two files
 */
public class HexDiff {

    /**
     * Compare the hex dumps of two files
     * @param reference     the file used as a base in the comparison
     * @param compared      the compared file
     *
     */
    public  HexDiff(File reference, File compared){
        this.reference = reference;
        this.compared = compared;
        this.refDiff = new TreeMap<>();
        this.comDiff = new TreeMap<>();
        this.oldBytes = new LinkedList<>();
        this.newBytes = new LinkedList<>();
        this.modifiedOffsets = new TreeSet<>();
        this.diffComputed = false;
        this.diffGenerator = new DiffService();
    }

    /**
     * The file used as a reference in the diff
     */
    private File reference;

    public File getReference() {
        return reference;
    }

    public void setReference(File reference) {
        if(this.reference.compareTo(reference) != 0) {
            this.reference = reference;
            this.diffComputed = false;
        }
    }

    /**
     * The file compared to the reference in the diff
     */
    private File compared;

    public File getCompared() {
        return compared;
    }

    public void setCompared(File compared) {
        if(this.compared.compareTo(compared) != 0) {
            this.compared = compared;
            this.diffComputed = false;
        }
    }

    /**
     * The list of modified offsets in the reference file
     * Operations listed are : EQUAL, DELETED
     */
    private SortedMap<Long, DiffUtils.Operation> refDiff;

    /**
     * The list of modified offsets in the compared file
     * Operations listed are : EQUAL, INSERTED
     */
    private SortedMap<Long, DiffUtils.Operation> comDiff;

    /**
     * The starting point of the line
     */
    private long offset;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * The number of lines the diff contains
     * One line is 16 bytes
     */
    private int nbLines;

    public int getNbLines() {
        return nbLines;
    }

    /**
     * The bytes of the old version, with their state
     */
    private LinkedList<ByteDiff> oldBytes;

    public LinkedList<ByteDiff> getOldBytes() {
        return oldBytes;
    }

    /**
     * The bytes of the new version, with their state
     */
    private LinkedList<ByteDiff> newBytes;

    public LinkedList<ByteDiff> getNewBytes() {
        return newBytes;
    }


    /**
     * The strings of the reference file
     */
    private String oldStrings;

    public String getOldStrings() {
        return oldStrings;
    }

    /**
     * The string of the compared file
     */
    private String newStrings;

    public String getNewStrings() {
        return newStrings;
    }

    /**
     * The list of offsets that were modified
     */
    private TreeSet<Long> modifiedOffsets;

    public TreeSet<Long> getModifiedOffsets() {
        return modifiedOffsets;
    }

    /**
     * Indicates if the complete diff has already been computed
     * If so it means that the set of modified offsets has already been generated
     * Otherwise whenever a diff is queried we will have to generate the complete diff
     * to get the modified offsets
     */
    private boolean diffComputed;

    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF GENERATION                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The service responsible for generating the diff
     */
    private HexDiff.DiffService diffGenerator;

    public HexDiff.DiffService getDiffGenerator() {
        return diffGenerator;
    }

    /**
     * Generate the diff for the specified files
     * @param reference     The file used as a reference in the diff
     * @param compared      The compared file in the diff
     * @param offset        the starting point for reading the files
     * @param nbLines       the number of hex lines to read. A line is 16 bytes.
     * @throws IOException
     */
    public void generateDiff(File reference, File compared, long offset, int nbLines) throws IOException {
        setReference(reference);
        setCompared(compared);
        loadDiff(offset, nbLines);
    }

    /**
     * Generate the diff if it hasn't been done yet and update the attributes storing the bytes and strings
     * @param offset        the starting point for reading the files
     * @param nbLines       the number of hex lines to read. A line is 16 bytes.
     * @throws IOException
     */
    public void loadDiff(long offset, int nbLines) throws IOException {
        setOffset(offset);
        oldBytes = new LinkedList<>();
        newBytes = new LinkedList<>();
        if(!diffComputed) {
            diffGenerator.setOnSucceeded(event -> {
                try {
                    updateDiff(offset, nbLines);
                } catch (IOException ignored) {
                }
            });
            diffGenerator.start();
        } else {
            updateDiff(offset, nbLines);
        }
    }

    /**
     * Update the diff bytes stored in the class to match the new offset and number of lines
     * @param offset        The new starting point of the diff to display
     * @param nbLines       The new number of line to display
     * @throws IOException
     */
    private void updateDiff(long offset, int nbLines) throws IOException {
        //We already have the diff stored in the maps refDiff and comDiff
        //We just need to read the files and update the appropriate attributes
        oldStrings = updateHexDump(reference, refDiff, oldBytes, offset, nbLines);
        newStrings = updateHexDump(compared, comDiff, newBytes, offset, nbLines);
        while (oldStrings.length() % 16 != 0) {
            oldStrings += ".";
        }
        while (newStrings.length() % 16 != 0) {
            newStrings += ".";
        }
        int maxChars = Math.max(oldStrings.length(), newStrings.length());
        this.nbLines = maxChars / 16;
    }


    /**
     * This function uses a map storing the diff of a file to read only a part of the file
     * and display the diff that part.
     * @param file      The file to read (either the reference or compared file)
     * @param map       The map storing the diff for that file
     * @param bytes     The list of ByteDiff that can be used to display the diff later on
     * @param offset    The offset from where to start reading
     * @param nbLines   The number of lines to read
     * @return          The ASCII strings that accompany the hex dump
     * @throws IOException
     */
    private String updateHexDump(File file,
                                 SortedMap<Long, DiffUtils.Operation> map,
                                 LinkedList<ByteDiff> bytes,
                                 long offset,
                                 int nbLines) throws IOException {
        //We start by determining the current operation
        Set<Long> keySet = map.keySet();
        Long[] keyOffsets = new Long[keySet.size()];
        final int[] i = {0};
        keySet.stream().forEach(aLong -> {
            keyOffsets[i[0]] = aLong;
            i[0]++;
        });
        int length = keyOffsets.length;
        int index = 0;
        boolean found = false;
        /* We are looking for the index such that keyOffsets[index] <= offset < keyOffset[index+1] */
        while(index<(length-1) && !found){
            if (keyOffsets[index] <= offset && offset < keyOffsets[index + 1])
                found = true;
            else
                index++;
        }
        DiffUtils.Operation currentOperation = map.get(keyOffsets[index]);

        //Init variables
        StringBuilder stringsBuilder = new StringBuilder();
        long currentOffset = offset;
        int remainingBytes = nbLines * HexDump.BYTES_PER_LINE;
        InputStream is = new FileInputStream(file);
        long skipped = is.skip(offset);
        if(skipped!=offset)
            currentOffset = skipped;

        //Start reading
        while(is.available()>0 && remainingBytes>0){
            int value = is.read();
            bytes.add(new ByteDiff(currentOperation, String.format("%02X", value)));
            if (!Character.isISOControl(value)){
                stringsBuilder.append((char) value);
            } else {
                stringsBuilder.append(".");
            }
            currentOffset++;
            if(index<(length-1)){
                if(currentOffset >= keyOffsets[index+1]){
                    index++;
                    currentOperation = map.get(keyOffsets[index]);
                }
            } else {
                currentOperation = map.get(keyOffsets[index]);
            }
            remainingBytes--;
        }

        return stringsBuilder.toString();
    }

    /**
     * Removes redundant information from the maps and set of modified offsets.
     */
    private void cleanupMap(SortedMap<Long, DiffUtils.Operation> map){
        DiffUtils.Operation previousOperation = null;
        //We factorize identical consecutive operations
        Iterator<Map.Entry<Long, DiffUtils.Operation>> iterator = map.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<Long, DiffUtils.Operation> entry = iterator.next();
            DiffUtils.Operation currentOperation = entry.getValue();
            if(currentOperation == previousOperation){
                iterator.remove();
            }
            previousOperation = currentOperation;
        }
    }

    /**
     * Removes consecutive offsets in the modifiedOffsets set
     */
    private void cleanupSet(){
        long previousOffset = -5;
        modifiedOffsets.stream()
                .filter(currentOffset -> (previousOffset + 1) == currentOffset)
                .forEach(modifiedOffsets::remove);
    }

    /**
     * This service is responsible for generating the diff of two files.
     * It must be run in a JavaFX application since it extends Service.
     * The DiffService provides information about the progress of the diff generation
     */
    public class DiffService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            return new DiffGeneratorTask();
        }
    }

    /**
     * The task responsible for generating the diff of two files
     */
    private class DiffGeneratorTask extends Task<Void> {

        @Override
        protected Void call() throws Exception {
            generateFullDiff();
            return null;
        }

        /**
         * This function generates a full diff between the reference file and the compared file
         * In doing so, the sets of modified offsets are being updated.
         */
        private void generateFullDiff() throws IOException {
            //To be able to handle big files we will cut the diff generation in portions of 1MB
            int MB = 1024*1024;
            int KB = 100*1024;
            long offsetMax = Math.max(reference.length(), compared.length());
            updateProgress(0, offsetMax);
            for(long offset = 0; offset<offsetMax; offset+=KB){
                computeDiff(offset, KB, offsetMax);
            }
            /* Remove redundancies */
            cleanupMap(refDiff);
            cleanupMap(comDiff);
            cleanupSet();
            diffComputed = true;
        }

        /**
         * Generates a diff for a specified amount of bytes
         * @param offset        The starting point of the diff
         * @param byteNumber    The number of byte to read to generate the diff
         */
        private void computeDiff(long offset, int byteNumber, long offsetMax) throws IOException {
        /* We need the hex dumps to generate the diff */
            String refDump = HexDump.getHexDump(reference, offset, byteNumber);
            String comDump = HexDump.getHexDump(compared, offset, byteNumber);
            DiffUtils diffUtils = new DiffUtils();
            List<DiffUtils.Diff> diff = diffUtils.diff_main(refDump, comDump);

        /* We update the maps and set of modified offsets from the results of the diff */
            DiffUtils.Operation oldOperationBuffer = null;
            DiffUtils.Operation newOperationBuffer = null;
            long currentOffsetOld = offset;
            long currentOffsetNew = offset;
            for(DiffUtils.Diff d: diff){
                int nibbleCount = d.text.length();
            /* If we have a nibble in store we need to join it to another nibble to create a byte */
                if(oldOperationBuffer != null || newOperationBuffer != null){
                    switch (d.operation){
                        case EQUAL:
                            if(oldOperationBuffer!=null){
                                refDiff.put(currentOffsetOld, oldOperationBuffer);
                                if(oldOperationBuffer!= DiffUtils.Operation.EQUAL)
                                    modifiedOffsets.add(currentOffsetOld);
                                currentOffsetOld++;
                                oldOperationBuffer = null;
                            }
                            if(newOperationBuffer!=null){
                                comDiff.put(currentOffsetNew, newOperationBuffer);
                                if(newOperationBuffer != DiffUtils.Operation.EQUAL)
                                    modifiedOffsets.add(currentOffsetNew);
                                currentOffsetNew++;
                                newOperationBuffer = null;
                            }
                            break;
                        case DELETE:
                            refDiff.put(currentOffsetOld, DiffUtils.Operation.DELETE);
                            modifiedOffsets.add(currentOffsetOld);
                            currentOffsetOld++;
                            oldOperationBuffer = null;
                            break;
                        case INSERT:
                            comDiff.put(currentOffsetNew, DiffUtils.Operation.INSERT);
                            modifiedOffsets.add(currentOffsetNew);
                            currentOffsetNew++;
                            newOperationBuffer = null;
                            break;
                    }
                    nibbleCount--;
                }
                if(nibbleCount>0){
                    switch (d.operation){
                        case EQUAL:
                            refDiff.put(currentOffsetOld, DiffUtils.Operation.EQUAL);
                            comDiff.put(currentOffsetNew, DiffUtils.Operation.EQUAL);
                            if(nibbleCount%2==0){
                                currentOffsetOld += nibbleCount/2;
                                currentOffsetNew += nibbleCount/2;
                            } else {
                                currentOffsetOld += (nibbleCount-1)/2;
                                currentOffsetNew += (nibbleCount-1)/2;
                                oldOperationBuffer = DiffUtils.Operation.EQUAL;
                                newOperationBuffer = DiffUtils.Operation.EQUAL;
                            }
                            break;
                        case DELETE:
                            refDiff.put(currentOffsetOld, DiffUtils.Operation.DELETE);
                            modifiedOffsets.add(currentOffsetOld);
                            if(nibbleCount%2==0){
                                currentOffsetOld += nibbleCount/2;
                            } else {
                                currentOffsetOld += (nibbleCount-1)/2;
                                oldOperationBuffer = DiffUtils.Operation.DELETE;
                            }
                            break;
                        case INSERT:
                            comDiff.put(currentOffsetNew, DiffUtils.Operation.INSERT);
                            modifiedOffsets.add(currentOffsetNew);
                            if(nibbleCount%2==0){
                                currentOffsetNew += nibbleCount/2;
                            } else {
                                currentOffsetNew += (nibbleCount-1)/2;
                                newOperationBuffer = DiffUtils.Operation.INSERT;
                            }
                            break;
                    }
                }
                updateProgress((currentOffsetNew+currentOffsetOld)/2, offsetMax);
            }
        }
    }
}
