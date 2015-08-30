package com.erwandano.hexviewer.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;

/**
 * A menu item with a check box
 */
public class CheckBoxMenuItem extends CustomMenuItem{

    /**
     * Indicates if the checkbox is selected
     */
    private BooleanProperty checked;


    public BooleanProperty selectedProperty(){
        return checkBox==null ? null : checkBox.selectedProperty();
    }

    public void setSelected(Boolean selected){
        checkBox.setSelected(selected);
    }

    public Boolean isSelected(){
        return checkBox.isSelected();
    }

    public BooleanProperty checkedProperty(){
        return checked;
    }

    public Boolean getChecked(){
        return this.checked.getValue();
    }

    public void setChecked(Boolean selected){
        this.checked.setValue(selected);
    }

    /**
     * Checkbox
     */
    private CheckBox checkBox;


    public CheckBoxMenuItem(){
        super();
        this.setHideOnClick(false);
        this.checked = new SimpleBooleanProperty();
        this.checkBox = new CheckBox();
        checkBox.textProperty().bind(this.textProperty());
        this.setContent(checkBox);
    }
}
