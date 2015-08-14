package hexviewer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.omg.PortableInterceptor.SUCCESSFUL;
import utils.HexDiff;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDiffBrowser extends HexBrowser {


    public HexDiffBrowser(){
        super();

        /* Master pane */
        this.referenceView = new HexDiffWebView();
        this.comparedView = new HexDiffWebView();
        this.splitPane = new SplitPane(referenceView, comparedView);
        this.progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(80);
        progressIndicator.setMaxHeight(80);
        StackPane stackPane = new StackPane(splitPane, progressIndicator);
        masterDetailPane.setMasterNode(stackPane);
        masterDetailPane.setDividerPosition(0.3);

        /* Detail pane */
        initDetailPane();

        //CTRL + SCROLL WHEEL TO ZOOM IN OR OUT
        EventHandler<ScrollEvent> scrollZoom = event -> {
            if (event.isControlDown()) {
                if (event.getDeltaY() > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
            }
        };

        referenceView.getWebView().setOnScroll(scrollZoom);
        comparedView.getWebView().setOnScroll(scrollZoom);
        linesPerPageTextField.setText(String.valueOf(linesPerPage));
    }


    /**
     * The file used as a reference in the diff
     */
    private File referenceFile;

    public File getReferenceFile() {
        return referenceFile;
    }

    public void setReferenceFile(File referenceFile) {
        this.referenceFile = referenceFile;
        if(comparedFile!=null)
            setOffsetMax(Math.max(referenceFile.length(), comparedFile.length()));
        else
            setOffsetMax(referenceFile.length());
    }

    /**
     * The file compared to the reference
     */
    private File comparedFile;

    public File getComparedFile() {
        return comparedFile;
    }

    public void setComparedFile(File comparedFile) {
        this.comparedFile = comparedFile;
        if(referenceFile!=null)
            setOffsetMax(Math.max(referenceFile.length(),comparedFile.length()));
        else
            setOffsetMax(comparedFile.length());
    }

    /**
     * The object holding the differences
     */
    private HexDiff hexDiff;


    /**
     * The hex viewer for the reference file
     */
    private HexDiffWebView referenceView;

    public HexDiffWebView getReferenceView() {
        return referenceView;
    }

    public void setReferenceView(HexDiffWebView referenceView) {
        this.referenceView = referenceView;
    }

    /**
     * The hex viewer for the compared file
     */
    private HexDiffWebView comparedView;

    public HexDiffWebView getComparedView() {
        return comparedView;
    }

    public void setComparedView(HexDiffWebView comparedView) {
        this.comparedView = comparedView;
    }

    public void loadDiff(File reference, File compared, long offset){
        setReferenceFile(reference);
        setComparedFile(compared);
        setOffset(offset);
        hexDiff = new HexDiff(reference,compared);
        progressIndicator.progressProperty().bind(hexDiff.getDiffGenerator().progressProperty());
        progressIndicator.setVisible(true);
        toolBar.setDisable(true);
        hexDiff.getDiffGenerator().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                progressIndicator.setVisible(false);
                this.referenceView.loadLines(hexDiff, true);
                this.comparedView.loadLines(hexDiff, false);
                setModifiedOffsets(hexDiff.getModifiedOffsets());
                toolBar.setDisable(false);
            }
        });
        try {
            hexDiff.loadDiff(offset, linesPerPage);
        } catch (IOException ignored) {}
    }

    /**
     * The splitpane separing the web views
     */
    private SplitPane splitPane;

    public SplitPane getSplitPane() {
        return splitPane;
    }

    /*******************************************************************************************************************
     *                                                                                                                 *
     * DETAIL PANE                                                                                                     *
     *                                                                                                                 *
     ******************************************************************************************************************/

    /**
     * We have to override the parent method to take into account the modified pages
     * @param linesPerPage
     */
    @Override
    public void setLinesPerPage(int linesPerPage) {
        this.linesPerPage = linesPerPage;
        linesPerPageTextField.setText(String.valueOf(linesPerPage));
        setPageMax((int) ((offsetMax / (linesPerPage * 32)) + 1));
        setPage((int) ((offset / (linesPerPage * 32)) + 1));
        setModifiedPages(hexDiff.getModifiedOffsets());
    }

    /**
     * The list of pages that were modified
     */
    private ObservableList<Integer> modifiedPages;

    private void setModifiedPages(SortedSet<Long> modifiedOffsets){
        modifiedPages.clear();
        SortedSet<Integer> pages = new TreeSet<>();
        for(Long modOffset : modifiedOffsets){
            double offsetd = (double) modOffset;
            double bytesPerPage = (double) linesPerPage*BYTES_PER_LINE;
            int page = (int) (Math.floor(offsetd/bytesPerPage) + 1);
            pages.add(page);
        }
        modifiedPages.setAll(pages);
    }


    /**
     * The list of offsets that were modified
     */
    private ObservableList<String> modifiedOffsets;

    private void setModifiedOffsets(SortedSet<Long> modifiedOffsets){
        this.modifiedOffsets.clear();
        int previousPage = 0;
        for(Long modOffset : modifiedOffsets){
            int page = (int) (Math.floorDiv(modOffset,linesPerPage*BYTES_PER_LINE) + 1);
            if(page!=previousPage){
                this.modifiedOffsets.add(String.format("%06X", modOffset));
            }
            previousPage = page;
        }
        setModifiedPages(modifiedOffsets);
    }

    /**
     * Initialize the detail pane
     */
    private void initDetailPane() {
        modifiedPages = FXCollections.observableArrayList();
        modifiedOffsets = FXCollections.observableArrayList();

        VBox detailPane = new VBox();

        /* List of modified pages and modified offsets */
        HBox modifiedPagesHbox = new HBox();
        HBox modifiedOffsetsHbox = new HBox();
        modifiedPagesHbox.setSpacing(10);
        modifiedOffsetsHbox.setSpacing(10);
        modifiedPagesHbox.setAlignment(Pos.BASELINE_CENTER);
        modifiedOffsetsHbox.setAlignment(Pos.BASELINE_CENTER);

        ListView<Integer> modifiedPagesList = new ListView<>(modifiedPages);
        ListView<String> modifiedOffsetsList = new ListView<>(modifiedOffsets);
        modifiedPagesList.setOrientation(Orientation.HORIZONTAL);
        modifiedOffsetsList.setOrientation(Orientation.HORIZONTAL);
        modifiedPagesList.setMaxHeight(55);
        modifiedOffsetsList.setMaxHeight(55);
        HBox.setHgrow(modifiedPagesList, Priority.ALWAYS);
        HBox.setHgrow(modifiedOffsetsList, Priority.ALWAYS);
        modifiedPagesList.setCellFactory(param -> new ModifiedPageCell());
        modifiedOffsetsList.setCellFactory(param -> new ModifiedOffsetCell());

        Label modifiedPagesLabel =   new Label("Modified pages  ");
        Label modifiedOffsetsLabel = new Label("Modified offsets");
        modifiedPagesLabel.setAlignment(Pos.BASELINE_CENTER);
        modifiedOffsetsLabel.setAlignment(Pos.BASELINE_CENTER);

        modifiedPagesHbox.getChildren().addAll(modifiedPagesLabel, modifiedPagesList);
        modifiedOffsetsHbox.getChildren().addAll(modifiedOffsetsLabel, modifiedOffsetsList);
        VBox modifiedBookmarks = new VBox(modifiedPagesHbox, modifiedOffsetsHbox);
        modifiedBookmarks.setSpacing(10);
        modifiedBookmarks.setFillWidth(true);
        modifiedBookmarks.setMaxHeight(120);
        VBox.setVgrow(modifiedBookmarks, Priority.NEVER);


        detailPane.getChildren().add(modifiedBookmarks);
        detailPane.setSpacing(10);
        detailPane.setPadding(new Insets(20));
        masterDetailPane.setDetailNode(detailPane);
    }

    /**
     * Allows the user to go to a modified page by double clicking on the item
     */
    private class ModifiedPageCell extends ListCell<Integer> {

        public ModifiedPageCell(){}

        @Override
        protected void updateItem(Integer item, boolean empty){
            super.updateItem(item, empty);
            if(item==null || empty){
                setText("");
            } else {
                setText(String.valueOf(item));
                this.setOnMouseClicked(event -> {
                    if(event.getClickCount()>=2){
                        gotoPage(item);
                    }
                });
            }
        }
    }

    /**
     * Allows the user to go to the page of a modified offset by double clicking on the offset
     */
    private class ModifiedOffsetCell extends ListCell<String> {

        public ModifiedOffsetCell(){}
        @Override
        protected void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            if(item==null || empty){
                setText("");
            } else {
                setText(String.valueOf(item));
                this.setOnMouseClicked(event -> {
                    if(event.getClickCount()>=2){
                        long offset = Long.parseLong(item, 16);
                        int page = (int) (( offset / (linesPerPage*BYTES_PER_LINE) ) + 1);
                        gotoPage(page);
                    }
                });
            }
        }
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
        try {
            hexDiff.loadDiff(offset, linesPerPage);
        } catch (IOException ignored) {}
        this.referenceView.loadLines(hexDiff, true);
        this.comparedView.loadLines(hexDiff, false);
    }

    /**
     * Zoom in : enlarge the webview's font
     */
    protected void zoomIn(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom + ZOOM_FACTOR);
    }


    /**
     * Zoom out : shrink the webview's font
     */
    protected void zoomOut(){
        Double currentZoom = referenceView.getWebView().getZoom();
        referenceView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
        comparedView.getWebView().zoomProperty().setValue(currentZoom - ZOOM_FACTOR);
    }



}
