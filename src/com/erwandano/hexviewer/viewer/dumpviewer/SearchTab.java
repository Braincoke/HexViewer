package com.erwandano.hexviewer.viewer.dumpviewer;

import com.erwandano.hexviewer.viewer.HexBrowser;
import javafx.scene.control.Tab;

/**
 * Search for string through the hex dump
 */
public class SearchTab extends Tab {

    //TODO

    public SearchTab(HexBrowser hexBrowser){
        this.hexBrowser = hexBrowser;
        setClosable(false);
    }

    /**
     * The parent HexBrowser
     */
    private HexBrowser hexBrowser;
}
