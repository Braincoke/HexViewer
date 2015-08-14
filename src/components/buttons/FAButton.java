package components.buttons;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

/**
 * FontAwesome Button
 * A Button that can display a FontAwesomeIcon as its graphic node
 */
public class FAButton extends Button {

    static {
        // Register a custom default font
        GlyphFontRegistry.register("FontAwesome", FAButton.class.getResourceAsStream("fontawesome.ttf"), 16);
    }
    protected GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");



    public FAButton(){
        super();
        this.icon = new SimpleStringProperty(this, "APPLE");
        this.size = new SimpleDoubleProperty(14);
        this.getStyleClass().add("fa-button");
    }

    /**
     * The glyph used
     */
    protected Glyph iconGlyph;

    /**
     * The icon size
     */
    protected DoubleProperty size;

    public double getSize() {
        return size.get();
    }

    public void setSize(double size){
        this.size.setValue(size);
        if(icon.getValue()!=null)
            this.setIcon(icon.getValue());
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    /**
     * The reference to the FontAwesome icon to load
     */
    protected StringProperty icon;


    public String getIcon() {
        return icon.get();
    }

    public void setIcon(String icon){
        this.icon.setValue(icon);
        for( FontAwesome.Glyph glyph : FontAwesome.Glyph.values()){
            if(glyph.name().compareToIgnoreCase(icon) == 0){
                double size = this.size == null ? 16 : this.size.getValue();
                char glyphChar = glyph.getChar();
                iconGlyph = fontAwesome.create(glyphChar);
                iconGlyph.size(size);
                iconGlyph.color(Color.BLACK);
                this.setGraphic(iconGlyph);
            }
        }
    }

    public StringProperty iconProperty() {
        return icon;
    }
}
