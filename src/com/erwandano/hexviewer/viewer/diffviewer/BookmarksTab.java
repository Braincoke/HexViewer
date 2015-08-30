package com.erwandano.hexviewer.viewer.diffviewer;


import com.erwandano.fxcomponents.buttons.FAButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A tab used to bookmark modified offsets and modified pages
 */
public class BookmarksTab extends Tab {


    public BookmarksTab(HexDiffBrowser hexDiffBrowser){
        setClosable(false);
        this.hexDiffBrowser = hexDiffBrowser;
        this.modifiedPages = FXCollections.observableArrayList();
        this.modifiedOffsets = FXCollections.observableArrayList();
        init();
    }


    /**
     * The parent HexDiffBrowser
     */
    private HexDiffBrowser hexDiffBrowser;

    /**
     * The list of pages that were modified
     */
    private ObservableList<Integer> modifiedPages;

    public void setModifiedPages(SortedSet<Long> modifiedOffsets){
        modifiedPages.clear();
        SortedSet<Integer> pages = new TreeSet<>();
        for(Long modOffset : modifiedOffsets){
            int page = hexDiffBrowser.offsetToPage(modOffset);
            pages.add(page);
        }
        modifiedPages.setAll(pages);
    }


    /**
     * The list of offsets that were modified
     */
    private ObservableList<String> modifiedOffsets;

    public void setModifiedOffsets(SortedSet<Long> pModifiedOffsets){
        this.modifiedOffsets.clear();
        int previousPage = 0;
        for(Long modOffset : pModifiedOffsets){
            int page = hexDiffBrowser.offsetToPage(modOffset);
            if(page!=previousPage){
                this.modifiedOffsets.add(String.format("%06X", modOffset));
            }
            previousPage = page;
        }
        setModifiedPages(pModifiedOffsets);
    }

    private void init(){
                /* First the offsets bookmarks */
        ListView<String> modifiedOffsetsList = new ListView<>(modifiedOffsets);
        modifiedOffsetsList.setCellFactory(param -> new ModifiedOffsetCell());
        Label modifiedOffsetsLabel = new Label("Modified offsets");
        modifiedOffsetsLabel.setLabelFor(modifiedOffsetsList);
        VBox offsetsBookmarks = new VBox(modifiedOffsetsLabel, modifiedOffsetsList);
        offsetsBookmarks.setSpacing(10);
        VBox.setVgrow(modifiedOffsetsList, Priority.ALWAYS);

        /* Then the pages bookmarks */
        ListView<Integer> modifiedPagesList = new ListView<>(modifiedPages);
        modifiedPagesList.setCellFactory(param -> new ModifiedPageCell());
        Label modifiedPagesLabel = new Label("Modified pages");
        modifiedPagesLabel.setLabelFor(modifiedPagesList);
        VBox pagesBookmarks = new VBox(modifiedPagesLabel, modifiedPagesList);
        pagesBookmarks.setSpacing(10);
        VBox.setVgrow(modifiedPagesList, Priority.ALWAYS);

        /* Combine the two */
        HBox splitHBox = new HBox(offsetsBookmarks, pagesBookmarks);
        splitHBox.setFillHeight(true);

        /* Add it to the bookmarks tab */
        this.setContent(splitHBox);

        /* Create the graphics for the tab */
        Label graphics = new Label("Bookmarks", FAButton.fontAwesome.create(FontAwesome.Glyph.BOOKMARK.getChar()));
        this.setGraphic(graphics);

        /* Handle tab change */
        graphics.setOnMouseClicked(event -> hexDiffBrowser.tabSelection(this));

        splitHBox.setId("bookmarks-tab-content");
        this.setId("bookmarks-tab");

    }


    /*******************************************************************************************************************
     *                                                                                                                 *
     * CUSTOM LIST CELLS                                                                                               *
     *                                                                                                                 *
     ******************************************************************************************************************/

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
                        hexDiffBrowser.gotoPage(item);
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
                        int page = hexDiffBrowser.offsetToPage(offset);
                        hexDiffBrowser.gotoPage(page);
                    }
                });
            }
        }
    }

}
