package registration;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import server.IServerRegistration;



public class RegistrationController implements Initializable{
    @FXML
    private Button disconnect_button;
    @FXML
    private TextField last_name_field;
    @FXML
    private TextField first_name_field;
    @FXML
    private TextField username_field;
    @FXML
    private PasswordField password_field;
    @FXML
    private Label user_message;
    @FXML
    private TextField field_board_name;
    @FXML
    private ComboBox choose_board;
    @FXML
    private Label board_message;

    private ArrayList<String> list_boards;
    public IServerRegistration iServerRegistration;

    
    public RegistrationController() {
        super();
    }

 
    // window initialization 
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        String url_rmi = "rmi://127.0.0.1/yechiel2";
        
        try {
            iServerRegistration = (IServerRegistration) Naming.lookup(url_rmi);
        } catch (Exception e) {}

        try {
            list_boards = iServerRegistration.displayList();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
     
        //initialization of error messages
        user_message.setText("");
        board_message.setText("");

    
        //initialization choose_board
        for (String board : list_boards) {
            choose_board.getItems().add(board);
        }

    }

    

    // For disconnection
    public void disconnectButtonOnAction() throws IOException {
        Stage stage = (Stage) disconnect_button.getScene().getWindow();
        stage.close();
    } 

    
    
    // Send user information to server
     public void addUserOnAction() throws RemoteException, SQLException {
       
        if(last_name_field.getText().isBlank() == true || username_field.getText().isBlank() == true || first_name_field.getText().isBlank() == true || password_field.getText().isBlank() == true || choose_board.getValue() == null ){
            user_message.setTextFill(Color.web("#FF3030"));
            user_message.setText("All fields are mandatory");
        }else{
            String[] reponse = iServerRegistration.addUserToTheDatabase(last_name_field.getText(), first_name_field.getText(), username_field.getText(), password_field.getText(), (String) choose_board.getValue());

            if(reponse[0].equals("error")){
                user_message.setTextFill(Color.web("#FF3030"));
            }else{
                user_message.setTextFill(Color.web("#006400"));

                //Initialize fields
                initializeAddUserFields();

            }
            user_message.setText(reponse[1]);
        }
    }

    

    // Send board information to server
    public void addBoardOnAction() throws RemoteException, SQLException {
        
        String board_name = field_board_name.getText();
        
        if (field_board_name.getText().isBlank() == true) {
            board_message.setTextFill(Color.web("#FF3030"));
            board_message.setText("This field is mandatory");
        } 
        else {
            String[] reponse = iServerRegistration.addBoardToTheDatabase(board_name);

            if (reponse[0].equals("error")) {
                board_message.setTextFill(Color.web("#FF3030"));
            } 
            else { 
                board_message.setTextFill(Color.web("#006400"));
            
                if(list_boards != null)
                    list_boards.clear();
                
                try {
                    list_boards = iServerRegistration.displayList();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                
                choose_board.getSelectionModel().clearSelection();
                choose_board.getItems().clear();
                
                for (String board : list_boards) {
                    choose_board.getItems().add(board);
                }
            
                //initialize field
                field_board_name.clear();
            }
            
            board_message.setText(reponse[1]);
            
        }
    }
    

    // Initialize all add user fields
    public void initializeAddUserFields(){
        last_name_field.clear();
        username_field.clear();
        first_name_field.clear();
        password_field.clear();
        choose_board.getSelectionModel().clearSelection();
    }
    
}