package com.erwandano.hexviewer.viewer;

/**
 * Available themes for the WebView
 */
public enum Theme {


    CLASSIC("Classic.css"),
    DARK("Dark.css");

    Theme(String filename){
        this.filename = filename;
    }

    private String filename;

    public String getFilename() {
        return filename;
    }
}
