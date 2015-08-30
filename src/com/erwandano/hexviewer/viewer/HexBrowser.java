package com.erwandano.hexviewer.viewer;


import com.erwandano.fxcomponents.buttons.FAButton;
import com.erwandano.fxcomponents.buttons.IconButton;
import com.erwandano.hexviewer.viewer.dumpviewer.SearchTab;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;

public abstract class HexBrowser extends AnchorPane {

    public static final int DEFAULT_LINE_NUMBER_PER_PAGE = 15;
    public static final int BYTES_PER_LINE = 16;
    public static final Double ZOOM_FACTOR = 0.10;

    protected HexBrowser(){
        linesPerPage = DEFAULT_LINE_NUMBER_PER_PAGE;
        /* VBox
         * |      ToolBar     |
         * |    SplitPane     |
         */
        this.toolBar = initToolBar();
        initMainSplitPane();
        VBox vBox = new VBox(toolBar, mainSplitPane);
        VBox.setVgrow(mainSplitPane, Priority.ALWAYS);
        this.getChildren().add(vBox);
        setRightAnchor(vBox, 0d);
        setBottomAnchor(vBox, 0d);
        setLeftAnchor(vBox, 0d);
        setTopAnchor(vBox, 0d);
    }

    /**
     * The current offset
     */
    protected long offset;

    public long getOffset() {
        return offset;
    }

    protected void setOffset(long offset) {
        setPage(offset);
        this.offset = offset;
    }


    /**
     * The number of lines displayed per page
     * A line contains 16 bytes
     */
    protected int linesPerPage;

    public int getLinesPerPage() {
        return linesPerPage;
    }

    public void setLinesPerPage(int linesPerPage) {
        this.linesPerPage = linesPerPage;
        linesPerPageTextField.setText(String.valueOf(linesPerPage));
        setPageMax(offsetMax);
        setPage(offset);
    }

    /**
     * The current page displayed
     * page = ( offset / (nbLinesPerPage*Bytes_per_line) ) + 1
     */
    protected int page;

    public int getPage() {
        return page;
    }

    protected void setPage(int page) {
        this.offset = (page-1)*linesPerPage*BYTES_PER_LINE;
        this.currentPageLabel.setText(String.valueOf(page));
        this.page = page;
    }

    protected void setPage(long offset){
        this.page = offsetToPage(offset);
        this.currentPageLabel.setText(String.valueOf(page));
    }

    /**
     * The last offset we can go to according
     * the file(s) length
     */
    protected long offsetMax;

    public long getOffsetMax() {
        return offsetMax;
    }

    protected void setOffsetMax(long offsetMax) {
        this.offsetMax = offsetMax;
        setPageMax(offsetToPage(offsetMax));
    }

    /**
     * The last page we can go to
     */
    protected int pageMax;

    public int getPageMax() {
        return pageMax;
    }

    protected void setPageMax(int pageMax){
        this.pageMax = pageMax;
        this.pageMaxLabel.setText(" / " + String.valueOf(pageMax));
    }

    protected void setPageMax(long offsetMax){
        this.pageMax = offsetToPage(offsetMax);
        this.pageMaxLabel.setText(" / " + String.valueOf(pageMax));
    }

    /**
     * Converts an offset to a page
     */
    public int offsetToPage(long offset){
        return (int) (( Math.floorDiv(offset,(linesPerPage*BYTES_PER_LINE) ) + 1));
    }

    /**
     * Converts a page to an offset
     */
    public long pageToOffset(int page){
        return (page-1)*linesPerPage*BYTES_PER_LINE;
    }


    /**
     * Loading indicator
     */
    protected ProgressIndicator progressIndicator;


    /*******************************************************************************************************************
     *                                                                                                                 *
     * TOOLBAR                                                                                                         *
     *  ______________________________________________________________________________________________________________ *
     * |                                               ______                          ______                         |*
     * | <-  currentPage / pageMax ->   |  Go to page |______| [Go]   |  Go to offset |______| [Go] | Lines per page  |*
     * |______________________________________________________________________________________________________________|*
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The toolbar used to change settings
     */
    protected ToolBar toolBar;

    public ToolBar getToolBar() {
        return toolBar;
    }

    public void setToolBar(ToolBar toolBar) {
        this.toolBar = toolBar;
    }

    /**
     * The current page displayed in the toolbar
     */
    protected Label currentPageLabel;

    /**
     * The last page that can be displayed
     */
    protected Label pageMaxLabel;

    /**
     * The number of lines displayed per page
     */
    protected TextField linesPerPageTextField;

    /**
     * Initialize the toolbar
     * @return the initialized toolbar
     */
    protected ToolBar initToolBar() {
        ToolBar toolBar = new ToolBar();

        //Page indicator
        IconButton previousPageBtn = new IconButton();
        previousPageBtn.setIcon("CHEVRON_LEFT");
        HBox pageIndicator = new HBox();
        currentPageLabel  = new Label();
        pageMaxLabel = new Label();
        IconButton nextPageBtn = new IconButton();
        nextPageBtn.setIcon("CHEVRON_RIGHT");
        pageIndicator.getChildren().addAll(currentPageLabel, pageMaxLabel);
        pageIndicator.setPadding(new Insets(0,10,0,10));
        pageIndicator.setAlignment(Pos.CENTER);
        toolBar.getItems().addAll(previousPageBtn, pageIndicator, nextPageBtn, new Separator());

        //Go to page
        Label gotoPageLabel = new Label("Go to page");
        TextField gotoPageTxt = new TextField();
        gotoPageTxt.setPrefWidth(50);
        gotoPageTxt.setAlignment(Pos.BASELINE_RIGHT);
        Button gotoPageBtn = new Button("Go");
        toolBar.getItems().addAll(gotoPageLabel, gotoPageTxt, gotoPageBtn, new Separator());

        //Go to offset
        Label gotoOffsetLabel = new Label("Go to offset");
        TextField gotoOffsetTxt = new TextField();
        gotoOffsetTxt.setPromptText("Offset in hex");
        gotoOffsetTxt.setAlignment(Pos.BASELINE_RIGHT);
        Button gotoOffsetBtn = new Button("Go");
        toolBar.getItems().addAll(gotoOffsetLabel, gotoOffsetTxt, gotoOffsetBtn, new Separator());

        //Set lines per page
        Label linesPerPageLabel = new Label("Lines per page");
        linesPerPageTextField = new TextField();
        linesPerPageTextField.setAlignment(Pos.BASELINE_RIGHT);
        linesPerPageTextField.setPrefWidth(40);
        toolBar.getItems().addAll(linesPerPageLabel, linesPerPageTextField, new Separator());

        //Zoom
        IconButton zoomInBtn = new IconButton();
        zoomInBtn.setIcon("SEARCH_PLUS");
        IconButton zoomOutBtn = new IconButton();
        zoomOutBtn.setIcon("SEARCH_MINUS");
        toolBar.getItems().addAll(zoomOutBtn, zoomInBtn);

        //Reload button
        IconButton reloadBtn = new IconButton();
        reloadBtn.setIcon("REFRESH");
        toolBar.getItems().add(reloadBtn);


        //Set actions
        previousPageBtn.setOnAction(event -> previousPage());
        nextPageBtn.setOnAction(event -> nextPage());
        try {
            gotoPageTxt.setOnAction(event -> gotoPage(Integer.parseInt(gotoPageTxt.getText())));
            gotoPageBtn.setOnAction(event -> gotoPage(Integer.parseInt(gotoPageTxt.getText())));
            gotoOffsetTxt.setOnAction(event -> gotoOffset(Long.parseLong(gotoOffsetTxt.getText(), 16)));
            gotoOffsetBtn.setOnAction(event -> gotoOffset(Long.parseLong(gotoOffsetTxt.getText(), 16)));
        }catch(NumberFormatException ignored){}
        linesPerPageTextField.setOnAction(event -> {
            setLinesPerPage(Integer.valueOf(linesPerPageTextField.getText()));
            reloadWebView();
        });
        zoomInBtn.setOnAction(event -> zoomIn());
        zoomOutBtn.setOnAction(event -> zoomOut());
        reloadBtn.setOnAction(event -> reloadWebView());
        return toolBar;
    }

    /**
     * The SplitPane dividing the content and the bottom tab
     */
    protected SplitPane mainSplitPane;

    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    /**
     * The saved dividers positions for the mainSplitPane
     */
    protected double savedMainDivider;

    /**
     * Dividers position that show everything
     */
    protected double defaultMainDivider = 0.33;

    /*******************************************************************************************************************
     *                                                                                                                 *
     * BOTTOM TAB PANE                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The tab pane at the bottom of the window
     */
    protected TabPane bottomTabPane;

    public TabPane getBottomTabPane() {
        return bottomTabPane;
    }

    /**
     * The currently selected tab;
     */
    protected Tab selectedTab;

    /**
     * The search tab that is included in every viewer
     */
    protected SearchTab searchTab;

    public SearchTab getSearchTab(){
        return searchTab;
    }



    /**
     * Indicates if the bottomTabPane is hidden
     */
    protected  boolean bottomTabHidden;

    /**
     * Below that threshold, the bottomTabPane is considered hidden
     */
    protected double hiddenThreshold = 0.85;

    public double getHiddenThreshold() {
        return hiddenThreshold;
    }

    public void setHiddenThreshold(double hiddenThreshold) {
        this.hiddenThreshold = hiddenThreshold;
    }

    /**
     * Show the bottomTabPane
     */
    protected void showBottomTabPane(){
        if(bottomTabHidden && savedMainDivider<hiddenThreshold) {
            mainSplitPane.setDividerPositions(savedMainDivider);
        }else if (bottomTabHidden && savedMainDivider>=hiddenThreshold) {
            mainSplitPane.setDividerPositions(defaultMainDivider);
        }
    }

    /**
     * Hide the bottomTabPane
     */
    public void collapseBottomTabPane() {
        if(!bottomTabHidden)
            savedMainDivider = mainSplitPane.getDividers().get(0).getPosition();
        mainSplitPane.setDividerPositions(1);
    }

    /**
     * Initialise the splitpane with the bottom tab
     * Initialise the search tab that every viewer should include
     */
    private void initMainSplitPane(){
        mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.VERTICAL);
        bottomTabPane = new TabPane();
        searchTab = new SearchTab(this);

        /* This "hack" is so that we can listen to mouse clicks on the tab
         * We use the graphics of the tab to display the text and the graphics
         * The node holding the graphics will listen to mouse clicks on the tab
         */
        Label label = new Label("Search", FAButton.fontAwesome.create(FontAwesome.Glyph.SEARCH.getChar()));
        searchTab.setGraphic(label);

        bottomTabPane.setSide(Side.BOTTOM);
        bottomTabPane.setMinHeight(20);
        bottomTabPane.getTabs().add(searchTab);
        selectedTab = searchTab;
        mainSplitPane.getItems().add(0, bottomTabPane);
        savedMainDivider = defaultMainDivider;

        /* Add style classes and IDs*/
        bottomTabPane.setId("bottom-tab-pane");
        searchTab.setId("search-tab");
        mainSplitPane.setId("main-split-pane");
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * NAVIGATION                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Go to the hex dump next page
     */
    protected void nextPage(){
        long newOffset = offset + linesPerPage*BYTES_PER_LINE;
        if(newOffset<offsetMax) {
            setOffset(offset + linesPerPage*BYTES_PER_LINE);
            reloadWebView();
        }
    }

    /**
     * Go to the previous hex dump page
     */
    protected void previousPage(){
        if(offset>=linesPerPage*BYTES_PER_LINE){
            setOffset(offset - linesPerPage*BYTES_PER_LINE);
            reloadWebView();
        }
    }

    /**
     * Go to the specified offset
     * @param newOffset    The offset to use as a start point when loading the hex view
     */
    protected void gotoOffset(long newOffset){
        if(newOffset<offsetMax && newOffset>=0){
            setOffset(newOffset);
            reloadWebView();
        }
    }

    /**
     * Go to the specified page
     * @param page  The page to load in the hex view
     */
    public void gotoPage(int page) {
        if(page<=pageMax && page>0){
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
    protected abstract void reloadWebView();

    /**
     * Zoom in : enlarge the webview's font
     */
    protected abstract void zoomIn();


    /**
     * Zoom out : shrink the webview's font
     */
    protected abstract void zoomOut();

    /**
     * Refresh the view
     */

}
