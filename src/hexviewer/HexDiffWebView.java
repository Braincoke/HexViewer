package hexviewer;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import utils.ByteDiff;
import utils.HexDiff;
import utils.HexDump;

import java.io.File;
import java.util.LinkedList;

/**
 * A web view to see the difference between two hex dumps
 */
public class HexDiffWebView extends StackPane {

    public HexDiffWebView(){
        super();
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

    public void loadLines(HexDiff diff, boolean isReference){
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.setUserStyleSheetLocation(getClass().getResource("HexDiffWebView.css").toExternalForm());
            }
        });
        webEngine.loadContent(generateHexView(diff, isReference));
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
     */
    private String generateHexView(HexDiff diff, boolean isReference){
        StringBuilder hexView = new StringBuilder("<div class=\"hex-view\">\n");
        hexView.append(getOffsetDiv(diff, isReference));
        hexView.append(getHexDiv(diff, isReference));
        hexView.append(getStringsDiv(diff, isReference));
        hexView.append("\n</div>\n");
        return hexView.toString();
    }

    private String getHexDiv(HexDiff diff, boolean isReference){
        LinkedList<ByteDiff> bytes;
        if(isReference)
            bytes = diff.getOldBytes();
        else
            bytes = diff.getNewBytes();
        String lineStart = "\t\t<div class=\"hex-dump-line\">\n";
        String lineEnd = "\n\t\t</div>\n";
        String spanStart = "<span class=\"hex-byte";
        String smallMargin = " small-margin-left";
        String largMargin = " large-margin-left";
        String matchedByte = " matched\">";
        String createdByte = " created\">";
        String deletedByte = " deleted\">";
        String spanEnd = "</span>";

        StringBuilder hexDiv = new StringBuilder();
        int bytePositionInLine = 0;
        for(int i=0; i<bytes.size(); i++) {
            ByteDiff byteDiff = bytes.get(i);
            switch (bytePositionInLine){
                case 0:
                    hexDiv.append(lineStart);
                    hexDiv.append(spanStart);
                    break;
                case 4:
                case 12:
                    hexDiv.append(spanStart).append(smallMargin);
                    break;
                case 8:
                    hexDiv.append(spanStart).append(largMargin);
                    break;
                default:
                    hexDiv.append(spanStart);
            }

            switch (byteDiff.getOperation()){
                case DELETE:
                    hexDiv.append(deletedByte);
                    break;
                case INSERT:
                    hexDiv.append(createdByte);
                    break;
                case EQUAL:
                    hexDiv.append(matchedByte);
                    break;
            }
            hexDiv.append(byteDiff.getByteHex());
            hexDiv.append(spanEnd);
            if(bytePositionInLine==15 || i==(bytes.size()-1)){
                hexDiv.append(lineEnd);
            }
            bytePositionInLine = (bytePositionInLine+1)%16;
        }
        return "\t<div class=\"hex-dump\">\n" + hexDiv.toString() + "\n\t</div>\n";
    }

    private String getStringsDiv(HexDiff diff, boolean isReference){
        StringBuilder stringsDiv = new StringBuilder();
        String strings;
        if(isReference)
            strings = diff.getOldStrings();
        else
            strings = diff.getNewStrings();
        String lineStart = "\t\t<div class=\"hex-strings-line\">\n\t";
        String lineEnd = "\n\t\t</div>\n";
        int lines = strings.length()/16;
        for(int i = 0; i<lines; i++){
            stringsDiv.append(lineStart).append(strings.substring(i*16,i*16+16)).append(lineEnd);
        }
        return "\t<div class=\"hex-strings\">\n" + stringsDiv.toString() + "\n\t</div>\n";
    }

    private String getOffsetDiv(HexDiff diff, boolean isReference){
        long offset = diff.getOffset();
        int nbLines = diff.getNbLines();
        long offsetMax = nbLines * HexDump.BYTES_PER_LINE +offset;
        StringBuilder offsetDiv = new StringBuilder("\t<div id=\"hexOffset\" class=\"hex-offset\">\n");
        for(long i= offset; i<offsetMax ; i+=HexDump.BYTES_PER_LINE){
            offsetDiv.append("\t\t<div class=\"hex-offset-line\">\n\t");
            offsetDiv.append(String.format("%06X",i));
            offsetDiv.append("\n\t\t</div>\n");
        }
        offsetDiv.append("\n\t</div>\n");
        return offsetDiv.toString();
    }
}
