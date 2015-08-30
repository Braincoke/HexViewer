import com.erwandano.hexviewer.viewer.dumpviewer.HexDumpBrowser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import com.erwandano.hexviewer.utils.HexDumpLine;
import com.erwandano.hexviewer.viewer.diffviewer.HexDiffBrowser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private HexDumpBrowser dumpBrowser;
    @FXML
    private HexDiffBrowser diffBrowser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private String getHexDump(List<HexDumpLine> lines){
        StringBuilder builder = new StringBuilder();
        for(HexDumpLine line : lines){
            builder.append(line.getHex());
        }
        return builder.toString();
    }

    public void loadDump(){
        String file1 = "File1.txt";
        File file = new File(file1);

        dumpBrowser.loadFile(file, 0);
        diffBrowser.setVisible(false);
    }

    public void loadDiff(){
        String bigPdfRef = "reference.pdf";
        String bigPdfCom = "compared.pdf";

        /* Hex Diff Testing */
        String resourcesPath = "test-resources/";
        String inserted = "inserted.pdf";
        String inserted1 = "inserted1.pdf";
        String inserted2 = "inserted2.pdf";
        String deleted = "deleted.pdf";
        String deleted1 = "deleted1.pdf";
        String deleted2 = "deleted2.pdf";
        String modified = "modified.pdf";
        String modified1 = "todo/modified1.pdf";
        String modified2 = "todo/modified2.pdf";
        String modified3 = "todo/modified3.pdf";

        String referencePath = resourcesPath + "todo/reference3.pdf";
        String comparedPath = resourcesPath + modified3;
        File reference = new File(referencePath);
        File compared = new File(comparedPath);
        assert(reference.exists());
        diffBrowser.loadDiff(reference,compared,0);
        dumpBrowser.setVisible(false);
    }

}
