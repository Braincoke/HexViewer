package com.erwandano.hexviewer.viewer.diffviewer;

import com.erwandano.hexviewer.components.buttons.FAButton;
import com.erwandano.hexviewer.utils.HexDiff;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A tab to modify the parameters of the HexDiff
 */
public class ParametersTab extends Tab{

    public ParametersTab(HexDiffBrowser hexDiffBrowser) {
        setClosable(false);
        this.hexDiffBrowser = hexDiffBrowser;
        init();
    }

    /**
     * The parent HexDiffBrowser
     */
    private HexDiffBrowser hexDiffBrowser;

    private void init(){
        HexDiff hexDiff = hexDiffBrowser.getHexDiff();

        /* First the window size parameters */
        //Label and TextField
        Label windowSizeLabel = new Label("Window size");
        TextField windowSizeTextField = new TextField();
        windowSizeTextField.setAlignment(Pos.BASELINE_RIGHT);
        //ComboBox for choosing the size unit
        ObservableList<String> windowSizeUnitList= FXCollections.observableArrayList("B", "KB", "MB");
        ComboBox<String> windowSizeUnit = new ComboBox<>(windowSizeUnitList);
        windowSizeUnit.getSelectionModel().select("KB");
        Label currentWSize = new Label("Current value = "
                + hexDiff.getWindowSize() + " "
                + hexDiff.getFormattedWindowSizeUnit());
        //Create the HBox
        HBox windowSizeHbox = new HBox(windowSizeLabel, windowSizeTextField, windowSizeUnit, currentWSize);
        windowSizeHbox.setAlignment(Pos.CENTER_LEFT);
        windowSizeHbox.setSpacing(10);

        /* Window step parameters */
        //Label and TextField
        Label windowStepLabel = new Label("Window step");
        TextField windowStepTextField = new TextField();
        windowStepTextField.setAlignment(Pos.BASELINE_RIGHT);
        //ComboBox for choosing the size unit
        ObservableList<String> windowStepUnitList= FXCollections.observableArrayList("B", "KB", "MB");
        ComboBox<String> windowStepUnit = new ComboBox<>(windowStepUnitList);
        windowStepUnit.getSelectionModel().select("KB");
        Label currentWStep = new Label("Current value = "
                + hexDiff.getWindowStep() + " "
                + hexDiff.getFormattedWindowStepUnit());
        //Create the HBox
        HBox windowStepHBox = new HBox(windowStepLabel, windowStepTextField, windowStepUnit, currentWStep);
        windowStepHBox.setAlignment(Pos.CENTER_LEFT);
        windowStepHBox.setSpacing(10);


        /* Button to apply the modifications */
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

        VBox diffParametersVBox = new VBox(windowSizeHbox, windowStepHBox, applyParameters);
        diffParametersVBox.setFillWidth(true);
        diffParametersVBox.setSpacing(20);
        diffParametersVBox.setAlignment(Pos.CENTER);

        diffParametersVBox.setId("parameters-tab-content");
        this.setContent(diffParametersVBox);

        /* Create the graphics */
        Label graphics = new Label("Parameters", FAButton.fontAwesome.create(FontAwesome.Glyph.COG.getChar()));
        this.setGraphic(graphics);

        /* Handle tab change events */
        graphics.setOnMouseClicked(event -> hexDiffBrowser.tabSelection(this));

        setId("parameters-tab");

    }
}
