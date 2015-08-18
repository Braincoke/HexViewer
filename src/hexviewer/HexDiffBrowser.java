package hexviewer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import utils.HexDiff;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple interface to browse the hex dump of a file
 */
public class HexDiffBrowser extends HexBrowser {


    public HexDiffBrowser(){
        super();

        /* Master pane */
        hexDiff = new HexDiff();
        this.referenceView = new HexDiffWebView();
        this.comparedView = new HexDiffWebView();
        this.splitPane = new SplitPane(referenceView, comparedView);
        this.progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(80);
        progressIndicator.setMaxHeight(80);
        HBox modalBackground = new HBox();
        modalBackground.setFillHeight(true);
        HBox.setHgrow(modalBackground, Priority.ALWAYS);
        modalBackground.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
        modalBackground.visibleProperty().bind(progressIndicator.visibleProperty());
        StackPane stackPane = new StackPane(splitPane, modalBackground, progressIndicator);
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

    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF PARAMETERS                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

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

    /*******************************************************************************************************************
     *                                                                                                                 *
     * DIFF GENERATION                                                                                                 *
     *                                                                                                                 *
     ******************************************************************************************************************/

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

    /**
     * Cancel loading a diff generation
     */
    public void cancel() {
        if(hexDiff.getDiffGenerator().getState()== Worker.State.RUNNING){
            hexDiff.getDiffGenerator().cancel();
        }
    }


    public void loadDiff(File reference, File compared, long offset){
        setReferenceFile(reference);
        setComparedFile(compared);
        setOffset(offset);
        hexDiff.setFiles(reference, compared);
        reloadDiff();
    }

    public void reloadDiff(){
        cancel();
        progressIndicator.progressProperty().unbind();
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
        } catch (IOException e) {
            Main.logger.log(Level.WARNING,
                    "Error when loading diff. \nFile 1: "
                            + referenceFile.toString()
                            + "\nFile 2: " + comparedFile.toString(),e);
        }
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
     * @param linesPerPage  The number of lines per page
     */
    @Override
    public void setLinesPerPage(int linesPerPage) {
        this.linesPerPage = linesPerPage;
        linesPerPageTextField.setText(String.valueOf(linesPerPage));
        setPageMax(offsetMax);
        setPage(offset);
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
            int page = offsetToPage(modOffset);
            pages.add(page);
        }
        modifiedPages.setAll(pages);
    }


    /**
     * The list of offsets that were modified
     */
    private ObservableList<String> modifiedOffsets;

    private void setModifiedOffsets(SortedSet<Long> pModifiedOffsets){
        this.modifiedOffsets.clear();
        int previousPage = 0;
        for(Long modOffset : pModifiedOffsets){
            int page = offsetToPage(modOffset);
            if(page!=previousPage){
                this.modifiedOffsets.add(String.format("%06X", modOffset));
            }
            previousPage = page;
        }
        setModifiedPages(pModifiedOffsets);
    }

    /**
     * Initialize the detail pane
     */
    private void initDetailPane() {
        modifiedPages = FXCollections.observableArrayList();
        modifiedOffsets = FXCollections.observableArrayList();

        VBox detailPane = new VBox();
        ScrollPane scrollPane = new ScrollPane(detailPane);
        scrollPane.setFitToWidth(true);
        /* List of modified pages and modified offsets */
        VBox modifiedBookmarks = initModifiedBookMarks();

        /* Parameters for the diff generation */
        TitledPane diffParameters = initParametersPane();

        detailPane.setFillWidth(true);
        detailPane.getChildren().addAll(modifiedBookmarks, diffParameters);
        detailPane.setSpacing(10);
        detailPane.setPadding(new Insets(0));
        masterDetailPane.setDetailNode(scrollPane);
    }

    /**
     * Create the VBox storing the list of modified pages and modified offsets
     * @return  The VBox
     */
    private VBox initModifiedBookMarks(){
        /* List of modified pages and modified offsets */
        HBox modifiedPagesHbox = new HBox();
        HBox modifiedOffsetsHbox = new HBox();
        HBox.setHgrow(modifiedPagesHbox, Priority.ALWAYS);
        HBox.setHgrow(modifiedOffsetsHbox, Priority.ALWAYS);
        modifiedPagesHbox.setSpacing(10);
        modifiedOffsetsHbox.setSpacing(10);
        modifiedPagesHbox.setAlignment(Pos.BASELINE_CENTER);
        modifiedOffsetsHbox.setAlignment(Pos.BASELINE_CENTER);

        ListView<Integer> modifiedPagesList = new ListView<>(modifiedPages);
        ListView<String> modifiedOffsetsList = new ListView<>(modifiedOffsets);
        modifiedPagesList.setOrientation(Orientation.HORIZONTAL);
        modifiedOffsetsList.setOrientation(Orientation.HORIZONTAL);
        modifiedPagesList.setMinHeight(45);
        modifiedOffsetsList.setMinHeight(45);
        modifiedPagesList.setPrefHeight(55);
        modifiedOffsetsList.setPrefHeight(55);
        modifiedPagesList.setMaxHeight(65);
        modifiedOffsetsList.setMaxHeight(65);
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
        modifiedBookmarks.setPadding(new Insets(20));
        VBox.setVgrow(modifiedBookmarks, Priority.NEVER);

        return modifiedBookmarks;
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
                        int page = offsetToPage(offset);
                        gotoPage(page);
                    }
                });
            }
        }
    }

    /**
     * Create the pane that lets the user modify the hexDiff parameters
     * @return  The titled pane
     */
    private TitledPane initParametersPane(){
        TitledPane diffParameters = new TitledPane();
        diffParameters.setText("Parameters");
        VBox diffParametersVBox = new VBox();
        diffParametersVBox.setFillWidth(true);
        diffParametersVBox.setSpacing(20);
        //WINDOW SIZE
        HBox windowSizeHbox = new HBox();
        Label windowSizeLabel = new Label("Window size");
        TextField windowSizeTextField = new TextField();
        windowSizeTextField.setAlignment(Pos.BASELINE_RIGHT);
        ObservableList<String> windowSizeUnitList= FXCollections.observableArrayList("B", "KB", "MB");
        ComboBox<String> windowSizeUnit = new ComboBox<>(windowSizeUnitList);
        windowSizeUnit.getSelectionModel().select("KB");
        Label currentWSize = new Label("Current value = "
                + hexDiff.getWindowSize() + " "
                + hexDiff.getFormattedWindowSizeUnit());
        windowSizeHbox.getChildren().setAll(windowSizeLabel, windowSizeTextField, windowSizeUnit, currentWSize);
        windowSizeHbox.setAlignment(Pos.CENTER_LEFT);
        windowSizeHbox.setSpacing(10);
        //WINDOW STEP
        HBox windowStepHBox = new HBox();
        Label windowStepLabel = new Label("Window step");
        TextField windowStepTextField = new TextField();
        windowStepTextField.setAlignment(Pos.BASELINE_RIGHT);
        ObservableList<String> windowStepUnitList= FXCollections.observableArrayList("B", "KB", "MB");
        ComboBox<String> windowStepUnit = new ComboBox<>(windowStepUnitList);
        windowStepUnit.getSelectionModel().select("KB");
        Label currentWStep = new Label("Current value = "
                + hexDiff.getWindowStep() + " "
                + hexDiff.getFormattedWindowStepUnit());
        windowStepHBox.getChildren().setAll(windowStepLabel, windowStepTextField, windowStepUnit, currentWStep);
        windowStepHBox.setAlignment(Pos.CENTER_LEFT);
        windowStepHBox.setSpacing(10);
        //Applying modifications
        Button applyParameters = new Button("Apply");
        applyParameters.setOnAction(event -> {
            //Apply modifications for the window size
            if (!windowSizeTextField.getText().trim().isEmpty()) {
                try {
                    long wSize = Long.parseLong(windowSizeTextField.getText());
                    String wSizeUnit = windowSizeUnit.getSelectionModel().getSelectedItem();
                    hexDiff.setWindowSize(wSize);
                    hexDiff.setWindowSizeUnit(wSizeUnit);
                    hexDiff.setDiffComputed(false);
                    currentWSize.setText("Current value = "
                            + hexDiff.getWindowSize() + " "
                            + hexDiff.getFormattedWindowSizeUnit());
                } catch (NumberFormatException e) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING,
                            "Error parsing the diff parameters. Long integer required.", e);
                }
            }
            if (!windowStepTextField.getText().trim().isEmpty()) {
                try {
                    long wStep = Long.parseLong(windowStepTextField.getText());
                    String wStepUnit = windowStepUnit.getSelectionModel().getSelectedItem();
                    hexDiff.setWindowStep(wStep);
                    hexDiff.setWindowStepUnit(wStepUnit);
                    hexDiff.setDiffComputed(false);
                    currentWStep.setText("Current value = "
                            + hexDiff.getWindowStep() + " "
                            + hexDiff.getFormattedWindowStepUnit());
                } catch (NumberFormatException e) {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING,
                            "Error parsing the diff parameters. Long integer required.", e);
                }
            }
        });
        diffParametersVBox.getChildren().setAll(windowSizeHbox, windowStepHBox, applyParameters);
        diffParameters.setContent(diffParametersVBox);
        return diffParameters;
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
        if(hexDiff.isDiffComputed()) {
            try {
                hexDiff.loadDiff(offset, linesPerPage);
            } catch (IOException ignored) {
            }
            this.referenceView.loadLines(hexDiff, true);
            this.comparedView.loadLines(hexDiff, false);
        } else {
            reloadDiff();
        }
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
