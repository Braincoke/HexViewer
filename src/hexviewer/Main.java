package hexviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class Main extends Application {

    public static Logger logger = Logger.getLogger("");

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("window.fxml"));
        primaryStage.setTitle("HexViewer");
        primaryStage.setScene(new Scene(root, 1200, 500));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
