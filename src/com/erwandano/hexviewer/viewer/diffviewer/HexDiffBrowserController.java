package com.erwandano.hexviewer.viewer.diffviewer;


import com.erwandano.fxcomponents.control.SplitTab;
import com.erwandano.hexviewer.utils.HexDiff;
import com.erwandano.hexviewer.viewer.HexBrowserController;
import com.erwandano.hexviewer.viewer.diffviewer.bookmarkstab.BookmarksTabController;
import com.erwandano.hexviewer.viewer.diffviewer.parameterstab.ParametersTabController;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDiffBrowserController extends HexBrowserController {


    public HexDiffBrowserController(){
        super();
        hexDiff = new HexDiff();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toolBarController.setHexBrowserController(this);
        //A modal overlay blocking user actions and showing the progress when loading a diff
        modalBackground.visibleProperty().bind(progressIndicator.visibleProperty());
        /* Configure user actions */
        //CTRL + SCROLL WHEEL TO ZOOM IN OR OUT
        EventHandler<ScrollEvent> scrollZoom = event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        };
        referenceView.getWebView().setOnScroll(scrollZoom);
        comparedView.getWebView().setOnScroll(scrollZoom);
        bookmarksTabController.setHexDiffBrowserController(this);
        parametersTabController.setHexDiffBrowserController(this);
    }

    /**
     * Loading indicator
     */
    @FXML
    protected ProgressIndicator progressIndicator;

    /**
     * Modal background
     */
    @FXML
    protected HBox modalBackground;

    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF PARAMETERS                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The file used as a reference in the diff
     */
    private File referenceFile;

    public File getReferenceFile() {
        return referenceFile;
    }

    public void setReferenceFile(File referenceFile) {
        this.referenceFile = referenceFile;
        if(comparedFile!=null)
            setOffsetMax(Math.max(referenceFile.length(), comparedFile.length()));
        else
            setOffsetMax(referenceFile.length());
    }

    /**
     * The file compared to the reference
     */
    private File comparedFile;

    public File getComparedFile() {
        return comparedFile;
    }

    public void setComparedFile(File comparedFile) {
        this.comparedFile = comparedFile;
        if(referenceFile!=null)
            setOffsetMax(Math.max(referenceFile.length(),comparedFile.length()));
        else
            setOffsetMax(comparedFile.length());
    }

    /**
     * We have to override the parent method to take into account the modified pages
     * @param linesPerPage  The number of lines per page
     */
    @Override
    public void setLinesPerPage(int linesPerPage) {
        this.linesPerPage.setValue(linesPerPage);
        setPageMax(getOffsetMax());
        setPage(getOffset());
        bookmarksTabController.setModifiedPages(hexDiff.getModifiedOffsets());
        reloadWebView();
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF GENERATION                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The object holding the differences
     */
    private HexDiff hexDiff;

    public HexDiff getHexDiff() {
        return hexDiff;
    }

    /**
     * The hex viewer for the reference file
     */
    @FXML
    private HexDiffWebView referenceView;

    public HexDiffWebView getReferenceView() {
        return referenceView;
    }

    public void setReferenceView(HexDiffWebView referenceView) {
        this.referenceView = referenceView;
    }

    /**
     * The hex viewer for the compared file
     */
    @FXML
    private HexDiffWebView comparedView;

    public HexDiffWebView getComparedView() {
        return comparedView;
    }

    public void setComparedView(HexDiffWebView comparedView) {
        this.comparedView = comparedView;
    }

    /**
     * Cancel loading a diff generation
     */
    public void cancel() {
        if(hexDiff.getDiffGenerator().getState()== Worker.State.RUNNING){
            hexDiff.getDiffGenerator().cancel();
        }
    }


    public void loadDiff(File reference, File compared, long offset){
        setReferenceFile(reference);
        setComparedFile(compared);
        setOffset(offset);
        hexDiff.setFiles(reference, compared);
        reloadDiff();
    }

    public void reloadDiff(){
        cancel();
        progressIndicator.progressProperty().unbind();
        progressIndicator.progressProperty().bind(hexDiff.getDiffGenerator().progressProperty());
        progressIndicator.setVisible(true);
        toolBar.setDisable(true);
        hexDiff.getDiffGenerator().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                progressIndicator.setVisible(false);
                this.referenceView.loadLines(hexDiff, true);
                this.comparedView.loadLines(hexDiff, false);
                bookmarksTabController.setModifiedOffsets(hexDiff.getModifiedOffsets());
                toolBar.setDisable(false);
            }
        });
        try {
            hexDiff.loadDiff(getOffset(), getLinesPerPage());
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Error when loading diff. \nFile 1: "
                            + referenceFile.toString()
                            + "\nFile 2: " + comparedFile.toString(), e);
        }
    }

    /**
     * The splitpane separing the web views
     */
    @FXML
    private SplitPane diffView;

    public SplitPane getDiffView() {
        return diffView;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * BOTTOM TABS                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    @FXML
    protected SplitTab bookmarksTab;
    @FXML
    protected BookmarksTabController bookmarksTabController;

    @FXML
    protected SplitTab parametersTab;
    @FXML
    protected ParametersTabController parametersTabController;


    /*******************************************************************************************************************
     *                                                                                                                 *
     * NAVIGATION                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/


    /**
     * Reload the hex view
     */
    public void reloadWebView(){
        if(hexDiff.isDiffComputed()) {
            try {
                hexDiff.loadDiff(getOffset(), getLinesPerPage());
            } catch (IOException ignored) {
            }
            this.referenceView.loadLines(hexDiff, true);
            this.comparedView.loadLines(hexDiff, false);
        } else {
            reloadDiff();
        }
    }

    /**
     * Zoom in : enlarge the webview's font
     */
    public void zoomIn(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
    }


    /**
     * Zoom out : shrink the webview's font
     */
    public void zoomOut(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
    }


}
