import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.logging.Logger;

public class Main extends Application {

    public static Logger logger = Logger.getLogger("");

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader();
        loader.setBuilderFactory(new JavaFXBuilderFactory());
        loader.setLocation((getClass().getResource("window.fxml")));
        InputStream in = getClass().getResourceAsStream("window.fxml");
        Parent root = loader.load(in);
        Controller controller = loader.getController();
        primaryStage.setTitle("HexViewer");
        primaryStage.setScene(new Scene(root, 1200, 500));

        controller.loadDump();

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
