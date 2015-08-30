package com.erwandano.hexviewer.components.buttons;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;

/**
 * Buttons for the Welcome Screen
 */
public class StartButton extends FAButton {

    public static double MAX_WIDTH = 350;

    public StartButton() {
        super();
        this.getStyleClass().add("startButton");
        this.setMaxWidth(MAX_WIDTH);
        this.setPadding(new Insets(15, 10, 15, 20));
        this.setFont(new Font(20));
        this.setGraphicTextGap(20);
        this.setAlignment(Pos.BASELINE_LEFT);
        setSize(20);
    }
}
