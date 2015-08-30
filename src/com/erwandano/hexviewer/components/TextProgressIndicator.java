package com.erwandano.hexviewer.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

/**
 * A progress indicator with a text beneath it
 */
public class TextProgressIndicator extends VBox {


    public TextProgressIndicator(){
        text = new SimpleStringProperty();
        progressIndicator = new ProgressIndicator();
        Label label = new Label();
        label.textProperty().bind(text);
        this.getChildren().addAll(progressIndicator, label);
    }

    /**
     * The text to display
     */
    private StringProperty text;

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    /**
     * The progress indicator
     */
    private ProgressIndicator progressIndicator;

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgress(double progress){
        progressIndicator.setProgress(progress);
    }

    public double getProgress(){
        return progressIndicator.getProgress();
    }

    public DoubleProperty progressProperty(){
        return progressIndicator.progressProperty();
    }
}
