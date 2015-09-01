package com.erwandano.hexviewer.viewer.toolbar;

import com.erwandano.hexviewer.viewer.HexBrowserController;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Control the HexBrowser toolbar
 */
public class ToolbarController {

    @FXML
    private Label currentPageLabel;
    @FXML
    private Label pageMaxLabel;
    @FXML
    private TextField linesPerPageTextField;
    @FXML
    private TextField gotoPageTextField;
    @FXML
    private TextField gotoOffsetTextField;

    private HexBrowserController hexBrowserController;

    public void setHexBrowserController(HexBrowserController browser){
        this.hexBrowserController = browser;
        currentPageLabel.textProperty().bind(Bindings.convert(hexBrowserController.pageProperty()));
        pageMaxLabel.textProperty().bind(Bindings.convert(hexBrowserController.pageMaxProperty()));
        linesPerPageTextField.setText(String.valueOf(hexBrowserController.getLinesPerPage()));
        hexBrowserController.linesPerPageProperty().addListener((observable, oldValue, newValue) ->
                linesPerPageTextField.setText(String.valueOf(newValue.intValue())));
    }


    public void previousPage() {
        hexBrowserController.previousPage();
    }

    public void nextPage(){
        hexBrowserController.nextPage();
    }

    public void gotoPage(){
        try{
            int page = Integer.parseInt(gotoPageTextField.getText());
            hexBrowserController.gotoPage(page);
        } catch (NumberFormatException ignore){}
    }

    public void gotoOffset(){
        try{
            long offset = Long.parseLong(gotoOffsetTextField.getText(), 16);
            hexBrowserController.gotoOffset(offset);
        } catch (NumberFormatException ignore){}
    }

    public void zoomIn(){
        hexBrowserController.zoomIn();
    }

    public void zoomOut(){
        hexBrowserController.zoomOut();
    }

    public void refresh(){
        hexBrowserController.reloadWebView();
    }

    public void setLinesPerPage() {
        try{
            int l = Integer.parseInt(linesPerPageTextField.getText());
            hexBrowserController.setLinesPerPage(l);
        }catch (NumberFormatException ignore){}
    }
}
