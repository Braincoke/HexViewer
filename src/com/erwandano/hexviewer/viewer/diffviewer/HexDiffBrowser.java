package com.erwandano.hexviewer.viewer.diffviewer;

import com.erwandano.hexviewer.utils.HexDiff;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *  A simple interface to browse the hex dump of a file
 *  This is a proxy class, the real controller is the HexDiffBrowserController
 */
public class HexDiffBrowser extends AnchorPane{

    public HexDiffBrowser(){
        super();
        /* Load the correct FXML */
        String fxml = "com/erwandano/hexviewer/viewer/diffviewer/HexDiffBrowser.fxml";
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(getClass().getClassLoader().getResource(fxml));
        InputStream is = getClass().getClassLoader().getResourceAsStream(fxml);
        StackPane stackPane;
        try {
            stackPane = loader.load(is);
            this.getChildren().add(stackPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = (HexDiffBrowserController) loader.getController();
    }

    /**
     * The browser controller
     */
    private HexDiffBrowserController controller;

    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF PARAMETERS                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The file used as a reference in the diff
     */
    public File getReferenceFile() {
        return controller.getReferenceFile();
    }

    public void setReferenceFile(File referenceFile) {
        controller.setReferenceFile(referenceFile);
    }

    /**
     * The file compared to the reference
     */
    public File getComparedFile() {
        return controller.getComparedFile();
    }

    public void setComparedFile(File comparedFile) {
        controller.setComparedFile(comparedFile);
    }

    /**
     * We have to override the parent method to take into account the modified pages
     * @param linesPerPage  The number of lines per page
     */
    public void setLinesPerPage(int linesPerPage) {
        controller.setLinesPerPage(linesPerPage);
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF GENERATION                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * The object holding the differences
     */
    public HexDiff getHexDiff() {
        return controller.getHexDiff();
    }

    /**
     * The hex viewer for the reference file
     */
    public HexDiffWebView getReferenceView() {
        return controller.getReferenceView();
    }

    public void setReferenceView(HexDiffWebView referenceView) {
        controller.setReferenceView(referenceView);
    }

    /**
     * The hex viewer for the compared file
     */
    public HexDiffWebView getComparedView() {
        return controller.getComparedView();
    }

    public void setComparedView(HexDiffWebView comparedView) {
        controller.setComparedView(comparedView);
    }

    /**
     * Cancel loading a diff generation
     */
    public void cancel() {
        controller.cancel();
    }


    public void loadDiff(File reference, File compared, long offset){
        controller.loadDiff(reference, compared, offset);
    }

    public void reloadDiff(){
        controller.reloadDiff();
    }

    /**
     * The splitpane separing the web views
     */
    public SplitPane getDiffView() {
        return controller.getDiffView();
    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * NAVIGATION                                                                                                      *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * Reload the hex view
     */
    public void reloadWebView(){
        controller.reloadWebView();
    }

    /**
     * Zoom in : enlarge the webview's font
     */
    public void zoomIn(){
        controller.zoomIn();
    }


    /**
     * Zoom out : shrink the webview's font
     */
    public void zoomOut(){
        controller.zoomOut();
    }

}
