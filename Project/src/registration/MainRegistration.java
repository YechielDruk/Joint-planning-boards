package registration;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class MainRegistration extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainRegistration.class.getResource("registration.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 280, 566);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args){
        launch();
    }
}
