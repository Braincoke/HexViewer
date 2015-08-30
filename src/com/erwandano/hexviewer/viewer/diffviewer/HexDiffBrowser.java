package com.erwandano.hexviewer.viewer.diffviewer;


import com.erwandano.hexviewer.utils.HexDiff;
import com.erwandano.hexviewer.viewer.HexBrowser;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDiffBrowser extends HexBrowser {


    public HexDiffBrowser(){
        super();

        hexDiff = new HexDiff();
        //The diff view to compare the two hex dumps side by side
        this.referenceView = new HexDiffWebView();
        this.comparedView = new HexDiffWebView();
        this.diffView = new SplitPane(referenceView, comparedView);
        //A modal overlay blocking user actions and showing the progress when loading a diff
        this.progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(80);
        progressIndicator.setMaxHeight(80);
        HBox modalBackground = new HBox();
        modalBackground.setFillHeight(true);
        HBox.setHgrow(modalBackground, Priority.ALWAYS);
        modalBackground.visibleProperty().bind(progressIndicator.visibleProperty());
        //Create the interface
        StackPane content = new StackPane(diffView, modalBackground, progressIndicator);
        mainSplitPane.getItems().add(0, content);

        /* Add tabs to the bottomTab */
        bookmarksTab = new BookmarksTab(this);
        bottomTabPane.getTabs().add(bookmarksTab);
        parametersTab = new ParametersTab(this);
        bottomTabPane.getTabs().add(parametersTab);

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
        linesPerPageTextField.setText(String.valueOf(linesPerPage));

        /* Save dividers position */
        mainSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            bottomTabHidden = newValue.doubleValue() >= hiddenThreshold;
        });
        mainSplitPane.setDividerPositions(1);

        /* Add style classes */
        modalBackground.getStyleClass().add("modal-background");
    }

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
        this.linesPerPage = linesPerPage;
        linesPerPageTextField.setText(String.valueOf(linesPerPage));
        setPageMax(offsetMax);
        setPage(offset);
        bookmarksTab.setModifiedPages(hexDiff.getModifiedOffsets());
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
                bookmarksTab.setModifiedOffsets(hexDiff.getModifiedOffsets());
                toolBar.setDisable(false);
            }
        });
        try {
            hexDiff.loadDiff(offset, linesPerPage);
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
    private SplitPane diffView;

    public SplitPane getDiffView() {
        return diffView;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * BOTTOM TABS                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    protected BookmarksTab bookmarksTab;

    protected ParametersTab parametersTab;


    public void tabSelection(Tab clickedTab) {
        //The user clicked on an already selected tab to hide or show the left menu
        if(selectedTab.getId().compareTo(clickedTab.getId())==0){
            if(bottomTabHidden) {
                bottomTabPane.getSelectionModel().getSelectedItem().getStyleClass().remove("hidden");
                showBottomTabPane();
            } else {
                bottomTabPane.getSelectionModel().getSelectedItem().getStyleClass().add("hidden");
                collapseBottomTabPane();
            }
        } else {
            //The user wants to change tabs
                bottomTabPane.getSelectionModel().getSelectedItem().getStyleClass().remove("hidden");
                bottomTabPane.getSelectionModel().select(clickedTab);
                selectedTab = clickedTab;
                showBottomTabPane();
        }
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * NAVIGATION                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/


    /**
     * Reload the hex view
     */
    protected void reloadWebView(){
        if(hexDiff.isDiffComputed()) {
            try {
                hexDiff.loadDiff(offset, linesPerPage);
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
    protected void zoomIn(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
    }


    /**
     * Zoom out : shrink the webview's font
     */
    protected void zoomOut(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
    }


}
