package com.erwandano.hexviewer.components.buttons;

import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

/**
 * An icon that also serves as a button. No background.
 * The icon is highlighted when hovered to indicate that some action is possible.
 */
public class IconButton extends FAButton {


    private Glyph hoveredGlyph;

    public IconButton(){
        super();
        this.getStyleClass().add("icon-button");
    }

    @Override
    public void setIcon(String icon){
        this.icon.setValue(icon);
        for( FontAwesome.Glyph glyph : FontAwesome.Glyph.values()){
            if(glyph.name().compareToIgnoreCase(icon) == 0){
                double size = this.size.getValue();
                char glyphChar = glyph.getChar();
                iconGlyph = fontAwesome.create(glyphChar);
                hoveredGlyph = fontAwesome.create(glyphChar);
                iconGlyph.size(size);
                hoveredGlyph.size(size);
                iconGlyph.color(Color.BLACK);
                hoveredGlyph.color(Color.GRAY);
                this.setGraphic(iconGlyph);
            }
            this.setOnMouseEntered(event -> this.setGraphic(hoveredGlyph));
            this.setOnMouseExited(event -> this.setGraphic(iconGlyph));
        }
    }
}
