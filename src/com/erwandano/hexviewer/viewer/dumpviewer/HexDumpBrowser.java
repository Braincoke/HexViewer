package com.erwandano.hexviewer.viewer.dumpviewer;

import com.erwandano.hexviewer.viewer.HexBrowser;

import java.io.File;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDumpBrowser extends HexBrowser {

    public HexDumpBrowser(){
        super();

        /* Master pane */
        this.webView = new HexDumpWebView();
        this.mainSplitPane.getItems().add(0, webView);

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

        /* Save dividers position */
        mainSplitPane.setDividerPositions(1);
        savedMainDivider = mainSplitPane.getDividers().get(0).getPosition();
        mainSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            bottomTabHidden = newValue.doubleValue() < hiddenThreshold;
        });

        linesPerPageTextField.setText(String.valueOf(linesPerPage));
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
     * The hex dump viewer
     */
    private HexDumpWebView webView;

    public HexDumpWebView getWebView() {
        return webView;
    }

    public void setWebView(HexDumpWebView webView) {
        this.webView = webView;
    }

    /**
     * Load a file in the hex dump viewer
     * @param file      The file to load
     * @param offset    The starting point of the hex dump
     */
    public void loadFile(File file, long offset){
        setFile(file);
        setOffset(offset);
        this.webView.loadLines(file, offset, linesPerPage);
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
        webView.loadLines(file, offset, linesPerPage);
    }

    /**
     * Zoom in : enlarge the webview's font
     */
    protected void zoomIn(){
        Double currentZoom = webView.getWebView().getZoom();
        webView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
    }


    /**
     * Zoom out : shrink the webview's font
     */
    protected void zoomOut(){
        Double currentZoom = webView.getWebView().getZoom();
        webView.getWebView().zoomProperty().setValue(currentZoom-ZOOM_FACTOR);
    }


}
