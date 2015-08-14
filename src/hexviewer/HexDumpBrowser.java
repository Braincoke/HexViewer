package hexviewer;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDumpBrowser extends HexBrowser {

    public HexDumpBrowser(){
        super();
        linesPerPage = DEFAULT_LINE_NUMBER_PER_PAGE;
        this.toolBar = initToolBar();
        this.webView = new HexDumpWebView();
        VBox vBox = new VBox(toolBar, webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
        this.getChildren().add(vBox);
        setRightAnchor(vBox, 0d);
        setBottomAnchor(vBox, 0d);
        setLeftAnchor(vBox, 0d);
        setTopAnchor(vBox, 0d);

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
