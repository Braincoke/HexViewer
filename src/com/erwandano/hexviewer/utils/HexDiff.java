package com.erwandano.hexviewer.utils;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a hex diff of two files
 */
public class HexDiff {

    public HexDiff(){
        this.windowSize = 100;
        this.windowSizeUnit = 1024;
        this.windowStep = 50;
        this.windowStepUnit = 1024;
        this.refDiff = new TreeMap<>();
        this.comDiff = new TreeMap<>();
        this.oldBytes = new LinkedList<>();
        this.newBytes = new LinkedList<>();
        this.modifiedOffsets = new TreeSet<>();
        this.diffComputed = false;
        this.diffGenerator = new DiffService();
    }


    /**
     * Initialise with files
     * @param reference     the file used as a base in the comparison
     * @param compared      the compared file
     *
     */
    public  HexDiff(File reference, File compared){
        this();
        this.reference = reference;
        this.compared = compared;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF PARAMETERS                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The file used as a reference in the diff
     */
    private File reference;

    public File getReference() {
        return reference;
    }

    public void setReference(File reference) {
        if(this.reference!=null) {
            if (this.reference.compareTo(reference) != 0) {
                this.reference = reference;
                this.diffComputed = false;
            }
        } else {
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
        if(this.compared != null) {
            if (this.compared.compareTo(compared) != 0) {
                this.compared = compared;
                this.diffComputed = false;
            }
        } else {
            this.compared = compared;
            this.diffComputed = false;
        }
    }

    /**
     * Set both file
     */
    public void setFiles(File reference, File compared){
        setReference(reference);
        setCompared(compared);
    }

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
     * The size of the sliding window for the diff generation
     */
    private long windowSize;

    public long getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(long windowSize) {
        this.windowSize = windowSize;
    }


    /**
     * The unit of the window size (B, KB, MB)
     */
    private long windowSizeUnit;

    public long getWindowSizeUnit() {
        return windowSizeUnit;
    }

    /**
     * Set the window size unit using the number of bytes in the unit (B = 1byte, KB = 1024 byte ...)
     * @param windowSizeUnit    The number of bytes in the unit
     */
    public void setWindowSizeUnit(long windowSizeUnit) {
        this.windowSizeUnit = windowSizeUnit;
    }

    /**
     * Set the window size unit using human readable unit (B for byte, KB for kilobyte, MB for megabyte)
     * @param windowSizeUnit    The human readable unit
     */
    public void setWindowSizeUnit(String windowSizeUnit) {
        switch (windowSizeUnit){
            case "B":
                this.windowSizeUnit = 1;
                break;
            case "KB":
                this.windowSizeUnit = 1024;
                break;
            case "MB":
                this.windowSizeUnit = 1024*1024;
                break;
            default:
                this.windowSizeUnit = 1024;
        }
    }


    /**
     * The number of offset to skip when sliding the window
     */
    private long windowStep;

    public long getWindowStep() {
        return windowStep;
    }

    public void setWindowStep(long windowStep) {
        this.windowStep = windowStep;
    }

    public String getFormattedWindowSizeUnit() {
        String formatted;
        if(windowSizeUnit == 1){
            formatted = "B";
        } else if (windowSizeUnit == 1024){
            formatted = "KB";
        } else if (windowSizeUnit == 1024*1024){
            formatted = "MB";
        } else {
            formatted = "?";
        }
        return  formatted;
    }

    /**
     * The unit of the window step
     */
    private long windowStepUnit;

    public long getWindowStepUnit() {
        return windowStepUnit;
    }

    public void setWindowStepUnit(long windowStepUnit) {
        this.windowStepUnit = windowStepUnit;
    }

    /**
     * Set the window step unit using human readable unit (B for byte, KB for kilobyte, MB for megabyte)
     * @param windowStepUnit    The human readable unit
     */
    public void setWindowStepUnit(String windowStepUnit) {
        switch (windowStepUnit){
            case "B":
                this.windowStepUnit = 1;
                break;
            case "KB":
                this.windowStepUnit = 1024;
                break;
            case "MB":
                this.windowStepUnit = 1024*1024;
                break;
            default:
                this.windowStepUnit = 1024;
        }
    }
    public String getFormattedWindowStepUnit() {
        String formatted;
        if(windowStepUnit == 1){
            formatted = "B";
        } else if (windowStepUnit == 1024){
            formatted = "KB";
        } else if (windowStepUnit == 1024*1024){
            formatted = "MB";
        } else {
            formatted = "?";
        }
        return  formatted;
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * GENERATED DIFF                                                                                                  *
     *                                                                                                                 *
     ******************************************************************************************************************/

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

    public boolean isDiffComputed() {
        return diffComputed;
    }

    public void setDiffComputed(boolean diffComputed) {
        this.diffComputed = diffComputed;
    }

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
     * Generate the diff if it hasn't been done yet and update the attributes storing the bytes and strings.
     * Allows customizing the window size and window step
     * @param offset            the starting point for reading the files
     * @param nbLines           the number of hex lines to read. A line is 16 bytes.
     * @param windowSize        the size of the sliding window
     * @param windowSizeUnit    the unit of the sliding window size
     * @param windowStep        the number of offsets skipped when sliding the window
     * @throws IOException
     */
    public void loadDiff(long offset, int nbLines, int windowSize, String windowSizeUnit, int windowStep) throws IOException {
        setOffset(offset);
        setWindowSize(windowSize);
        setWindowSizeUnit(windowSizeUnit);
        setWindowStep(windowStep);
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
            modifiedOffsets = new TreeSet<>();
            diffGenerator.setOnSucceeded(event -> {
                try {
                    updateDiff(offset, nbLines);
                } catch (IOException ignored) {
                }
            });
            diffGenerator.reset();
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

        /**
         * First offset of the window
         */
        private long windowStart;

        /**
         * Last offset of the window
         */
        private long windowEnd;

        /**
         * The number of bytes associated to the current operation
         */
        private int byteCount;

        private long shift;

        private long offsetMax;

        @Override
        protected Void call() throws Exception {
            //Logger.getLogger(getClass().getName()).log(Level.INFO, "Starting diff generation");
            shift = 0;
            generateFullDiff();
            return null;
        }

        /**
         * Verify that we are not adding noise generated by the chunked diff and update the map accordingly
         * @param map           Either refDiff or comDiff
         * @param offsetDiff    The offset to put in the map
         * @param operationDiff The operation to put in the map
         */
        private void addDiff(SortedMap<Long, DiffUtils.Operation> map,
                             Long offsetDiff,
                             DiffUtils.Operation operationDiff){
            boolean addDiff = false;

            /* If the shift is negative we will tend to see inserted lines for the last offset of the chunk
             * and deleted lines for the first offset of the chunk
             */
            if(shift<0) {
            /* Is it the very first chunk or the very last one? */
                if (windowStart == 0) {
                /* Then we add everything as long as it is not an insert going up to the last offset */
                    if (operationDiff == DiffUtils.Operation.INSERT) {
                        if (offsetDiff + byteCount < windowEnd) {
                            addDiff = true;
                        }
                    } else {
                        addDiff = true;
                    }
                } else if (windowEnd >= offsetMax) {
                /* Then we add everything as long as it is not a deletion related to the first offset */
                    if (operationDiff == DiffUtils.Operation.DELETE) {
                        if (offsetDiff > windowStart) {
                            addDiff = true;
                        }
                    } else {
                        addDiff = true;
                    }
                } else {
                /* Then we add only the "middle" */
                    if ((operationDiff == DiffUtils.Operation.INSERT && (offsetDiff + byteCount < windowEnd))
                            || (operationDiff == DiffUtils.Operation.DELETE && (offsetDiff > windowStart))) {
                        addDiff = true;
                    } else if (operationDiff == DiffUtils.Operation.EQUAL){
                        addDiff = true;
                    }
                }
            } else if (shift>0){
                /* If the shift if positive we will tend to see deleted lines for the last offset of the chunk
                 * and inserted lines for the first offset of the chunk
                 */

            /* Is it the very first chunk or the very last one? */
                if (windowStart == 0) {
                /* Then we add everything as long as it is not a deletion going up to the last offset */
                    if (operationDiff == DiffUtils.Operation.DELETE) {
                        if (offsetDiff + byteCount < windowEnd) {
                            addDiff = true;
                        }
                    } else {
                        addDiff = true;
                    }
                } else if (windowEnd >= offsetMax) {
                /* Then we add everything as long as it is not an insertion related to the first offset */
                    if (operationDiff == DiffUtils.Operation.INSERT) {
                        if (offsetDiff > windowStart) {
                            addDiff = true;
                        }
                    } else {
                        addDiff = true;
                    }
                } else {
                /* Then we add only the "middle" */
                    if ((operationDiff == DiffUtils.Operation.DELETE && (offsetDiff + byteCount < windowEnd))
                            || (operationDiff == DiffUtils.Operation.INSERT && (offsetDiff > windowStart))) {
                        addDiff = true;
                    } else if (operationDiff== DiffUtils.Operation.EQUAL){
                        addDiff = true;
                    }
                }
            } else {
                addDiff = true;
            }

            if(addDiff){
                map.put(offsetDiff, operationDiff);
                switch (operationDiff){
                    case DELETE:
                        modifiedOffsets.add(offsetDiff);
                        shift -= byteCount;
                        break;
                    case INSERT:
                        modifiedOffsets.add(offsetDiff);
                        shift += byteCount;
                        break;
                }
            }
        }

        /**
         * This function generates a full diff between the reference file and the compared file
         * In doing so, the sets of modified offsets are being updated.
         */
        private void generateFullDiff() throws IOException {
            //To be able to handle big files we will cut the diff generation in portions of x*KB or x*MB
            offsetMax = Math.max(reference.length(), compared.length());
            updateProgress(0, offsetMax);
            long currentOffset = 0;
            while(currentOffset < offsetMax && !isCancelled()){
                windowStart = currentOffset;
                windowEnd = currentOffset+ windowSize*windowSizeUnit;
                computeDiff(currentOffset, windowSize*windowSizeUnit);
                currentOffset += windowStep*windowStepUnit;
                updateProgress(currentOffset, offsetMax);
            }
            if(!isCancelled()) {
            /* Remove redundancies */
                cleanupMap(refDiff);
                cleanupMap(comDiff);
                cleanupSet();
                diffComputed = true;
            } else {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Diff generation was cancelled");
            }
        }

        /**
         * Generates a diff for a specified amount of bytes
         * @param offset        The starting point of the diff
         * @param byteNumber    The number of byte to read to generate the diff
         */
        private void computeDiff(long offset, long byteNumber) throws IOException {
        /* We need the hex dumps to generate the diff */
            String refDump = HexDump.getHexDump(reference, offset, byteNumber);
            String comDump = HexDump.getHexDump(compared, offset, byteNumber);
            DiffUtils diffUtils = new DiffUtils();
            LinkedList<DiffUtils.Diff> diff = diffUtils.diff_main(refDump, comDump);
            //diffUtils.diff_cleanupSemantic(diff);
        /* We update the maps and set of modified offsets from the results of the diff */
            DiffUtils.Operation oldOperationBuffer = null;
            DiffUtils.Operation newOperationBuffer = null;
            long currentOffsetOld = offset;
            long currentOffsetNew = offset;
            for(DiffUtils.Diff d: diff){
                int nibbleCount = d.text.length();
            /* If we have a nibble in store we need to join it to another nibble to create a byte */
                if(oldOperationBuffer != null || newOperationBuffer != null){
                    byteCount = 1;
                    switch (d.operation){
                        case EQUAL:
                            if(oldOperationBuffer!=null){
                                addDiff(refDiff,currentOffsetOld, oldOperationBuffer);
                                currentOffsetOld++;
                                oldOperationBuffer = null;
                            }
                            if(newOperationBuffer!=null){
                                addDiff(comDiff, currentOffsetNew, newOperationBuffer);
                                currentOffsetNew++;
                                newOperationBuffer = null;
                            }
                            break;
                        case DELETE:
                            addDiff(refDiff, currentOffsetOld, DiffUtils.Operation.DELETE);
                            currentOffsetOld++;
                            oldOperationBuffer = null;
                            break;
                        case INSERT:
                            addDiff(comDiff, currentOffsetNew, DiffUtils.Operation.INSERT);
                            currentOffsetNew++;
                            newOperationBuffer = null;
                            break;
                    }
                    nibbleCount--;
                }
                if(nibbleCount>0){
                    if(nibbleCount%2==0)
                        byteCount = nibbleCount/2;
                    else
                        byteCount = (nibbleCount-1)/2;
                    switch (d.operation){
                        case EQUAL:
                            addDiff(refDiff, currentOffsetOld, DiffUtils.Operation.EQUAL);
                            addDiff(comDiff, currentOffsetNew, DiffUtils.Operation.EQUAL);
                            currentOffsetOld += byteCount;
                            currentOffsetNew +=  byteCount;
                            if(nibbleCount%2!=0){
                                oldOperationBuffer = DiffUtils.Operation.EQUAL;
                                newOperationBuffer = DiffUtils.Operation.EQUAL;
                            }
                            break;
                        case DELETE:
                            addDiff(refDiff, currentOffsetOld, DiffUtils.Operation.DELETE);
                            if(nibbleCount%2==0){
                                currentOffsetOld += byteCount;
                            } else {
                                currentOffsetOld += byteCount;
                                oldOperationBuffer = DiffUtils.Operation.DELETE;
                            }
                            break;
                        case INSERT:
                            addDiff(comDiff, currentOffsetNew, DiffUtils.Operation.INSERT);
                            if(nibbleCount%2==0){
                                currentOffsetNew += byteCount;
                            } else {
                                currentOffsetNew += byteCount;
                                newOperationBuffer = DiffUtils.Operation.INSERT;
                            }
                            break;
                    }
                }
//                updateProgress((currentOffsetNew+currentOffsetOld)/2, offsetMax);
            }
        }
    }
}
