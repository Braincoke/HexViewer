package com.erwandano.hexviewer.viewer.diffviewer.parameterstab;

import com.erwandano.hexviewer.utils.HexDiff;
import com.erwandano.hexviewer.viewer.diffviewer.HexDiffBrowserController;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controls the parameters tab for the HexDiff object
 */
public class ParametersTabController {


    @FXML
    private TextField windowSize;

    @FXML
    private TextField windowStep;

    @FXML
    private ComboBox<String> windowSizeUnit;

    @FXML
    private ComboBox<String> windowStepUnit;

    @FXML
    private Label currentSize;

    @FXML
    private Label currentStep;

    private HexDiffBrowserController hexDiffBrowserController;

    private HexDiff hexDiff;

    public void setHexDiffBrowserController(HexDiffBrowserController hexDiffBrowserController){
        this.hexDiffBrowserController = hexDiffBrowserController;
        this.hexDiff = hexDiffBrowserController.getHexDiff();
        currentSize.setText("Current value = " + hexDiff.getWindowSize() + " " + hexDiff.getFormattedWindowSizeUnit());
        currentStep.setText("Current value = " + hexDiff.getWindowStep() + " " + hexDiff.getFormattedWindowStepUnit());
    }

    public void setHexDiffParameters() {
        //Apply modifications for the window size
        if (!windowSize.getText().trim().isEmpty()) {
            try {
                long wSize = Long.parseLong(windowSize.getText());
                String wSizeUnit = windowSizeUnit.getSelectionModel().getSelectedItem();
                hexDiff.setWindowSize(wSize);
                hexDiff.setWindowSizeUnit(wSizeUnit);
                hexDiff.setDiffComputed(false);
                currentSize.setText("Current value = "
                        + hexDiff.getWindowSize() + " "
                        + hexDiff.getFormattedWindowSizeUnit());
            } catch (NumberFormatException e) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Error parsing the diff parameters. Long integer required.", e);
            }
        }
        if (!windowStep.getText().trim().isEmpty()) {
            try {
                long wStep = Long.parseLong(windowStep.getText());
                String wStepUnit = windowStepUnit.getSelectionModel().getSelectedItem();
                hexDiff.setWindowStep(wStep);
                hexDiff.setWindowStepUnit(wStepUnit);
                hexDiff.setDiffComputed(false);
                currentStep.setText("Current value = "
                        + hexDiff.getWindowStep() + " "
                        + hexDiff.getFormattedWindowStepUnit());
            } catch (NumberFormatException e) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING,
                        "Error parsing the diff parameters. Long integer required.", e);
            }
        }
    }
}
