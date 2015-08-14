package hexviewer;

import components.buttons.IconButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MasterDetailPane;

public abstract class HexBrowser extends AnchorPane {

    public static final int DEFAULT_LINE_NUMBER_PER_PAGE = 15;
    public static final int BYTES_PER_LINE = 16;
    public static final Double ZOOM_FACTOR = 0.10;

    protected HexBrowser(){
        linesPerPage = DEFAULT_LINE_NUMBER_PER_PAGE;
        /* VBox
         * |      ToolBar     |
         * | MasterDetailPane |
         */
        this.toolBar = initToolBar();
        this.masterDetailPane = new MasterDetailPane();
        this.masterDetailPane.setShowDetailNode(false);
        this.masterDetailPane.setAnimated(true);
        this.masterDetailPane.setDetailSide(Side.TOP);
        VBox vBox = new VBox(toolBar, masterDetailPane);
        VBox.setVgrow(masterDetailPane, Priority.ALWAYS);
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
        setPage((int) ((offset/(linesPerPage*BYTES_PER_LINE))+1));
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
        setPageMax((int) ((offsetMax / (linesPerPage * BYTES_PER_LINE)) + 1));
        setPage((int) ((offset / (linesPerPage * BYTES_PER_LINE)) + 1));
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
        setPageMax((int) (( offsetMax/ (linesPerPage*BYTES_PER_LINE) ) + 1));
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

        //Hamburger
        IconButton hamburgerBtn = new IconButton();
        hamburgerBtn.setIcon("BARS");
        HBox hbox = new HBox(hamburgerBtn);
        hbox.setAlignment(Pos.BASELINE_RIGHT);
        HBox.setHgrow(hbox, Priority.ALWAYS);
        toolBar.getItems().add(hbox);

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
        hamburgerBtn.setOnAction(event -> toggleDetailPane());
        return toolBar;
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * MASTER DETAIL PANE                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The main pane
     */
    protected MasterDetailPane masterDetailPane;

    public MasterDetailPane getMasterDetailPane() {
        return masterDetailPane;
    }

    public void setMasterDetailPane(MasterDetailPane masterDetailPane) {
        this.masterDetailPane = masterDetailPane;
    }

    protected void toggleDetailPane(){
        masterDetailPane.setShowDetailNode(!masterDetailPane.isShowDetailNode());
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
    protected void gotoPage(int page) {
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


}
