package com.erwandano.hexviewer.viewer.dumpviewer;

import com.erwandano.hexviewer.viewer.HexBrowserController;
import javafx.scene.control.Tab;

/**
 * Search for string through the hex dump
 */
public class SearchTab extends Tab {

    //TODO

    public SearchTab(HexBrowserController hexBrowserController){
        this.hexBrowserController = hexBrowserController;
        setClosable(false);
        setId("search-tab");
    }

    /**
     * The parent HexBrowser
     */
    private HexBrowserController hexBrowserController;
}
