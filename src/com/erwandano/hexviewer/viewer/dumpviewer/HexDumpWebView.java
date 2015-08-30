package com.erwandano.hexviewer.viewer.dumpviewer;

import com.erwandano.hexviewer.viewer.Theme;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import com.erwandano.hexviewer.utils.HexDump;
import com.erwandano.hexviewer.utils.HexDumpLine;

import java.io.File;
import java.util.List;

/**
 * Displays a hex dump in a web view
 */
public class HexDumpWebView extends StackPane{

    public static final Theme DEFAULT_THEME = Theme.DARK;

    public HexDumpWebView(){
        super();
        theme = DEFAULT_THEME;
        webView = new WebView();
        webEngine = webView.getEngine();
        getChildren().add(webView);
        webView.setContextMenuEnabled(false);
    }


    /**
     * The web view that will display the html
     */
    private WebView webView;

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    /**
     * The web engine
     */
    private WebEngine webEngine;

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public void setWebEngine(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    /**
     * Path to the stylesheet to use for the webEngine
     */
    private Theme theme;

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void loadLines(File file, long offset, int nbLines){
        List<HexDumpLine> hexDumpLines = HexDump.getLines(file, offset, nbLines);
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.setUserStyleSheetLocation(getClass().getClassLoader().getResource(
                        "com/erwandano/hexviewer/resources/css/hexdumpwebview/" + theme.getFilename()
                ).toExternalForm());
            }
        });
        webEngine.loadContent(generateHexView(hexDumpLines));
    }

    /**
     * Generate a div containing the hex view:
     * <pre>
     * <div class="hex-view">
     *      <div class="hex-offset"></div>
     *      <div class="hex-dump"></div>
     *      <div class="hex-strings"></div>
     * </div>
     * </pre>
     * @param lines
     * @return
     */
    public String generateHexView(List<HexDumpLine> lines){

        //First we create the div for the offsets
        StringBuilder offsetDiv = new StringBuilder("<div id=\"hexOffset\" class=\"hex-offset\">\n");
        for(HexDumpLine line : lines){
            offsetDiv.append("<div class=\"hex-offset-line\">");
            offsetDiv.append(String.format("%06X", line.getOffset()));
            offsetDiv.append("</div>");
        }
        offsetDiv.append("\n</div>");

        //Then we create the hex dump div
        StringBuilder hexdumpDiv = new StringBuilder("<div class=\"hex-dump\">\n");
        for(HexDumpLine line : lines) {
            StringBuilder hexdumpLine = new StringBuilder("<div class=\"hex-dump-line\">\n");
            String hex = line.getHex();
            int length = hex.length() - 1; //length == 31
            for (int i = 0; i < length; i += 2) {
                switch (i % 16) {
                    case 0:
                        hexdumpLine.append("<span class=\"hex-byte large-margin-left\" >");
                        break;
                    case 8:
                        hexdumpLine.append("<span class=\"hex-byte small-margin-left\" >");
                        break;
                    default:
                        hexdumpLine.append("<span class=\"hex-byte\" >");
                        break;
                }
                hexdumpLine.append(hex.substring(i, i + 2));
                hexdumpLine.append("</span>");
            }
            hexdumpLine.append("\n</div>");
            hexdumpDiv.append(hexdumpLine);
        }
        hexdumpDiv.append("</div>");

        //Now for the strings
        StringBuilder stringsDiv = new StringBuilder("<div class=\"hex-strings\">\n");
        for(HexDumpLine line : lines){
            StringBuilder stringLine = new StringBuilder("<div class=\"hex-strings-line\">\n");
            stringLine.append(line.getStrings()).append("\n");
            stringLine.append("</div>\n");
            stringsDiv.append(stringLine);
        }
        stringsDiv.append("</div>\n");

        //Build the hexview
        return "<div class=\"hex-view\">\n" + offsetDiv + hexdumpDiv + stringsDiv + "\n</div>";
    }
}
