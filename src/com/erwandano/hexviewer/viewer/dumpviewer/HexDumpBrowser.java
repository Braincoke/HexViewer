package com.erwandano.hexviewer.viewer.dumpviewer;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple interface to browse the hex dump of a file
 * This is a proxy class the real controller is the HexDumpBrowserController
 */
public class HexDumpBrowser extends AnchorPane{

    public HexDumpBrowser(){
        super();
        /* Load the correct FXML */
        String fxml = "com/erwandano/hexviewer/viewer/dumpviewer/HexDumpBrowser.fxml";
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation(getClass().getClassLoader().getResource(fxml));
        InputStream is = getClass().getClassLoader().getResourceAsStream(fxml);
        VBox vBox;
        try {
            vBox = loader.load(is);
            this.getChildren().add(vBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = (HexDumpBrowserController) loader.getController();
    }

    /**
     * The browser controller
     */
    private HexDumpBrowserController controller;

    /**
     * The file which will be displayed
     */

    public File getFile() {
        return controller.getFile();
    }

    public void setFile(File file) {
        controller.setFile(file);
    }

    /**
     * The hex dump viewer
     */
    public HexDumpWebView getWebView() {
        return controller.getWebView();
    }

    public void setWebView(HexDumpWebView webView) {
        controller.setWebView(webView);
    }

    /**
     * Load a file in the hex dump viewer
     * @param file      The file to load
     * @param offset    The starting point of the hex dump
     */
    public void loadFile(File file, long offset){
        controller.loadFile(file, offset);
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
