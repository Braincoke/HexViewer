package com.erwandano.hexviewer.viewer;


import com.erwandano.fxcomponents.control.SplitTabPane;
import com.erwandano.hexviewer.viewer.dumpviewer.SearchTab;
import com.erwandano.hexviewer.viewer.toolbar.ToolbarController;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;

public abstract class HexBrowserController implements Initializable {

    public static final int DEFAULT_LINE_NUMBER_PER_PAGE = 15;
    public static final int BYTES_PER_LINE = 16;
    public static final Double ZOOM_FACTOR = 0.10;

    protected HexBrowserController(){
        linesPerPage = new SimpleIntegerProperty();
        pageMax = new SimpleIntegerProperty();
        page = new SimpleIntegerProperty();
        offset = new SimpleLongProperty();
        offsetMax = new SimpleLongProperty();
        linesPerPage.setValue(DEFAULT_LINE_NUMBER_PER_PAGE);
    }

    /**
     * The parent node
     */
    @FXML
    protected AnchorPane node;

    public AnchorPane getNode() {
        return node;
    }

    /**
     * The current offset
     */
    protected LongProperty offset;

    public ReadOnlyLongProperty offsetProperty(){
        return ReadOnlyLongProperty.readOnlyLongProperty(offset);
    }

    public long getOffset() {
        return offset.getValue();
    }

    protected void setOffset(long offset) {
        setPage(offset);
        this.offset.setValue(offset);
    }


    /**
     * The number of lines displayed per page
     * A line contains 16 bytes
     */
    protected IntegerProperty linesPerPage;

    public ReadOnlyIntegerProperty linesPerPageProperty(){
        return ReadOnlyIntegerProperty.readOnlyIntegerProperty(linesPerPage);
    }

    public int getLinesPerPage() {
        return linesPerPage.getValue();
    }

    public void setLinesPerPage(int linesPerPage) {
        this.linesPerPage.setValue(linesPerPage);
        setPageMax(getOffsetMax());
        setPage(getOffset());
        reloadWebView();
    }

    /**
     * The current page displayed
     * page = ( offset / (nbLinesPerPage*Bytes_per_line) ) + 1
     */
    protected IntegerProperty page;

    public ReadOnlyIntegerProperty pageProperty(){
        return ReadOnlyIntegerProperty.readOnlyIntegerProperty(page);
    }

    public int getPage() {
        return page.getValue();
    }

    protected void setPage(int page) {
        this.offset.setValue(pageToOffset(page));
        this.page.setValue(page);
    }

    protected void setPage(long offset){
        this.page.setValue(offsetToPage(offset));
    }

    /**
     * The last offset we can go to according
     * the file(s) length
     */
    protected LongProperty offsetMax;

    public ReadOnlyLongProperty offsetMaxProperty(){
        return ReadOnlyLongProperty.readOnlyLongProperty(offsetMax);
    }

    public long getOffsetMax() {
        return offsetMax.getValue();
    }

    protected void setOffsetMax(long offsetMax) {
        this.offsetMax.setValue(offsetMax);
        setPageMax(offsetToPage(offsetMax));
    }

    /**
     * The last page we can go to
     */
    protected IntegerProperty pageMax;

    public int getPageMax() {
        return pageMax.getValue();
    }

    public ReadOnlyIntegerProperty pageMaxProperty(){
        return ReadOnlyIntegerProperty.readOnlyIntegerProperty(pageMax);
    }

    protected void setPageMax(int pageMax){
        this.pageMax.setValue(pageMax);
    }

    protected void setPageMax(long offsetMax){
        this.pageMax.setValue(offsetToPage(offsetMax));
    }

    /**
     * Converts an offset to a page
     */
    public int offsetToPage(long offset){
        return (int) (( Math.floorDiv(offset,(getLinesPerPage()*BYTES_PER_LINE) ) + 1));
    }

    /**
     * Converts a page to an offset
     */
    public long pageToOffset(int page){
        return (page-1)*getLinesPerPage()*BYTES_PER_LINE;
    }


    /**
     * The SplitPane dividing the content and the bottom tab
     */
    @FXML
    protected SplitTabPane splitTabPane;

    public SplitTabPane getSplitTabPane() {
        return splitTabPane;
    }

    /**
     * The toolbar
     */
    @FXML
    protected ToolBar toolBar;
    @FXML
    protected ToolbarController toolBarController;

    /*******************************************************************************************************************
     *                                                                                                                 *
     * BOTTOM TAB PANE                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The search tab that is included in every viewer
     */
    protected SearchTab searchTab;

    public SearchTab getSearchTab(){
        return searchTab;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * NAVIGATION                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Go to the hex dump next page
     */
    public void nextPage(){
        long newOffset = getOffset() + getLinesPerPage()*BYTES_PER_LINE;
        if(newOffset<getOffsetMax()) {
            setOffset(newOffset);
            reloadWebView();
        }
    }

    /**
     * Go to the previous hex dump page
     */
    public void previousPage(){
        long newOffset = getOffset() - getLinesPerPage()*BYTES_PER_LINE;
        if(newOffset>=0){
            setOffset(newOffset);
            reloadWebView();
        }
    }

    /**
     * Go to the specified offset
     * @param newOffset    The offset to use as a start point when loading the hex view
     */
    public void gotoOffset(long newOffset){
        if(newOffset<getOffsetMax() && newOffset>=0){
            setOffset(newOffset);
            reloadWebView();
        }
    }

    /**
     * Go to the specified page
     * @param page  The page to load in the hex view
     */
    public void gotoPage(int page) {
        if(page<=getPageMax() && page>0){
            setPage(page);
            reloadWebView();
        }
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * ABSTRACT METHODS                                                                                                *
     *                                                                                                                 *
     ******************************************************************************************************************/



    /**
     * Reload the hex view
     */
    public abstract void reloadWebView();

    /**
     * Zoom in : enlarge the webview's font
     */
    public abstract void zoomIn();


    /**
     * Zoom out : shrink the webview's font
     */
    public abstract void zoomOut();

}
