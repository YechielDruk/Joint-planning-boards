package users;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.sql.SQLException;
import java.util.ResourceBundle;
import server.IServerUsers;
import static users.BoardController.username;
import static users.BoardController.board_name;


public class LogInController implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private TextField usernameInput;
    @FXML
    private PasswordField passwordInput;
    @FXML
    private Label errorMessageLogIn;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        // Pressing enter in the password field activates the log in button
        passwordInput.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    logInButtonOnAction();
                } catch (IOException | NotBoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    // For disconnection
    public void cancelButtonOnAction() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }

    
    // Verify log in details and log in
    public void logInButtonOnAction() throws IOException, NotBoundException, SQLException {
        
        if(usernameInput.getText().isBlank() == true || passwordInput.getText().isBlank() == true)
            errorMessageLogIn.setText("Fill the fields first");
            
        else{
            String url = "rmi://127.0.0.1/yechiel";
            IServerUsers iServerUsers = (IServerUsers) Naming.lookup(url);
            
            String[] reponse = iServerUsers.logIn(usernameInput.getText(),passwordInput.getText());
            String errorOrSuccess = reponse[0];
            String message = reponse[1]; 
            
            if(errorOrSuccess.equals("error"))
                errorMessageLogIn.setText(message);
            
            else{
                username = usernameInput.getText();
                board_name = reponse[1];
                
                Stage stage = (Stage) cancelButton.getScene().getWindow();
                stage.close();

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("board.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 760, 520);
                Stage newStage = new Stage();
                newStage.setScene(scene);
                newStage.show();
            }
        }
    }
}