package com.erwandano.hexviewer.components.buttons;

import javafx.scene.text.Font;

/**
 * A classic toolbar button : no background except when hovered
 */
public class ToolBarButton extends FAButton {

    public ToolBarButton() {
        super();
        this.getStyleClass().add("toolbarButton");
        this.getStyleClass().add("no-focus");
        this.setFont(Font.font(14));
    }
}
