package com.erwandano.hexviewer.utils;

import java.nio.file.Path;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Search for file times inside a hexdump
 */
public abstract class FileTime {


    /**
     * The path to the file which will produce the hexdump
     */
    protected Path path;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    /**
     * The list of offsets where a match was found
     */
    protected SortedSet<Long> searchResults;

    public SortedSet<Long> getSearchResults(){
        return this.searchResults;
    }

    public FileTime(){
        searchResults = new TreeSet<>();
    }

    public FileTime(Path path){
        this();
        this.path = path;
    }

}
