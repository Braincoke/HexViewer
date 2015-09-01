package com.erwandano.hexviewer.viewer.dumpviewer;

import com.erwandano.hexviewer.viewer.HexBrowserController;
import javafx.fxml.FXML;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the HexDumpBrowser fxml
 */
public class HexDumpBrowserController extends HexBrowserController{

    public HexDumpBrowserController(){
        super();

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toolBarController.setHexBrowserController(this);
        //CTRL + SCROLL WHEEL TO ZOOM IN OR OUT
        webView.getWebView().setOnScroll(event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        });
    }

    /**
     * The hex dump viewer
     */
    @FXML
    private HexDumpWebView webView;

    public HexDumpWebView getWebView() {
        return webView;
    }

    public void setWebView(HexDumpWebView webView) {
        this.webView = webView;
    }


    /**
     * The file which will be displayed
     */
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        setOffsetMax(file.length());
    }

    /**
     * Load a file in the hex dump viewer
     * @param file      The file to load
     * @param offset    The starting point of the hex dump
     */
    public void loadFile(File file, long offset){
        setFile(file);
        setOffset(offset);
        this.webView.loadLines(file, offset, getLinesPerPage());
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
        webView.loadLines(file, getOffset(), getLinesPerPage());
    }

    /**
     * Zoom in : enlarge the webview's font
     */
    public void zoomIn(){
        Double currentZoom = webView.getWebView().getZoom();
        webView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
    }


    /**
     * Zoom out : shrink the webview's font
     */
    public void zoomOut(){
        Double currentZoom = webView.getWebView().getZoom();
        webView.getWebView().zoomProperty().setValue(currentZoom-ZOOM_FACTOR);
    }
}
