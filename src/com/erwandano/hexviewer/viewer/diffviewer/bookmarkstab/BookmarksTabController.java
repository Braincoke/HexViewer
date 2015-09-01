package com.erwandano.hexviewer.viewer.diffviewer.bookmarkstab;

import com.erwandano.hexviewer.viewer.diffviewer.HexDiffBrowserController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Controller for the bookmarks tab.
 */
public class BookmarksTabController implements Initializable {


    @FXML
    private ListView<String> modifiedOffsetsListView;

    @FXML
    private ListView<Integer> modifiedPagesListView;


    public BookmarksTabController(){
        modifiedOffsets = FXCollections.observableArrayList();
        modifiedPages = FXCollections.observableArrayList();
    }

    /**
     * The parent HexDiffBrowser
     */
    private HexDiffBrowserController hexDiffBrowserController;

    public void setHexDiffBrowserController(HexDiffBrowserController browser){
        this.hexDiffBrowserController = browser;
        modifiedOffsetsListView.setItems(modifiedOffsets);
        modifiedPagesListView.setItems(modifiedPages);
        modifiedOffsetsListView.setCellFactory(param -> new ModifiedOffsetCell());
        modifiedPagesListView.setCellFactory(param -> new ModifiedPageCell());
    }

    /**
     * The list of pages that were modified
     */
    private ObservableList<Integer> modifiedPages;

    public void setModifiedPages(SortedSet<Long> modifiedOffsets){
        modifiedPages.clear();
        SortedSet<Integer> pages = new TreeSet<>();
        for(Long modOffset : modifiedOffsets){
            int page = hexDiffBrowserController.offsetToPage(modOffset);
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
            int page = hexDiffBrowserController.offsetToPage(modOffset);
            if(page!=previousPage){
                this.modifiedOffsets.add(String.format("%06X", modOffset));
            }
            previousPage = page;
        }
        setModifiedPages(pModifiedOffsets);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
                        hexDiffBrowserController.gotoPage(item);
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
                        int page = hexDiffBrowserController.offsetToPage(offset);
                        hexDiffBrowserController.gotoPage(page);
                    }
                });
            }
        }
    }

}
