package hexviewer;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import utils.HexDumpLine;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private HexDumpWebView hexDumpWebView;
    @FXML
    private HexDiffBrowser browser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String file1 = "File1.txt";
        File file = new File(file1);
        File reference = new File("reference.pdf");
        File compared = new File("compared.pdf");
        //browser.loadFile(reference, 0);
        browser.loadDiff(reference,compared,0);
    }

    private String getHexDump(List<HexDumpLine> lines){
        StringBuilder builder = new StringBuilder();
        for(HexDumpLine line : lines){
            builder.append(line.getHex());
        }
        return builder.toString();
    }

}
