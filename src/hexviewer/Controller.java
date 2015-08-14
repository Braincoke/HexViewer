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

        String bigPdfRef = "reference.pdf";
        String bigPdfCom = "compared.pdf";

        /* Hex Diff Testing */
        String resourcesPath = "test-resources/";
        String referencePath = resourcesPath + "reference.pdf";
        String inserted = "inserted.pdf";
        String inserted1 = "inserted1.pdf";
        String inserted2 = "inserted2.pdf";
        String deleted = "deleted.pdf";
        String deleted1 = "deleted1.pdf";
        String deleted2 = "deleted2.pdf";
        String modified = "modified.pdf";
        String modified1 = "modified1.pdf";
        String modified2 = "modified2.pdf";
        String comparedPath = resourcesPath + modified2;
        File reference = new File(bigPdfRef);
        File compared = new File(bigPdfCom);
        assert(reference.exists());
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
