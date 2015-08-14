# HexViewer
A JavaFX application to view hex dumps and hex diffs

This JavaFX application can be used as a module in another application to view hex dumps or hex diffs. 

## Dependencies

* ControlsFX
* diff_match_patch : this dependency is already included in the source code. The class has been renamed DiffUtils.
* Java 8 update 51

## How to use

You can use the classes HexDiffBrowser or HexDumpBrowser that provides a full environment to easily navigate through the pages. 

    HexDiffBrowser browser = new HexDiffBrowser();
    parentNode.getChildren().add(browser);
    File reference = new File("reference.pdf");
    File compared = new File("compared.pdf");
    browser.loadDiff(reference, compared, 0);
