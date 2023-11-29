package users;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.*;
import java.util.Stack;
import server.IServerUsers;


public class BoardController extends UnicastRemoteObject implements IUsers, Initializable{
    @FXML
    private Label discussionLabel;
    @FXML
    private Button disconnectButton;
    @FXML
    private ListView<String> listDisplayUsers;
    @FXML
    public VBox vBoxMessages;
    @FXML
    private TextField messageInput;
    @FXML
    private Canvas canvas;
    @FXML
    private HBox buttonBox;
    @FXML
    private HBox buttonBox2;
    @FXML
    public ScrollPane messageTextArea;
    @FXML
    private Button labelButton;

    public static String username = "";
    public static String board_name = "";
    public String username_destination = "";
    private ArrayList<UsersDiscussions> users_channel = new ArrayList<UsersDiscussions>();
    
    // By default
    String type="Pen";
    Color color = Color.BLACK;
    double lineWidth = 1;
    
    boolean first_undo=false;
    double x_position_click=0;
    double y_position_click=0;
    
    final Button resetButton = new Button("Delete all");
    final Button redoButton = new Button("Redo");
    final Button undoButton = new Button("Undo");
    Stack<Image> images_undo = new Stack<>();
    Stack<Image> images_redo = new Stack<>();
    Image current_image;
    Image last_image;

    public IServerUsers iServerUsers;
    public GraphicsContext graphicsContext;
    FileChooser fileChooser = new FileChooser();
    
    
    public BoardController() throws RemoteException {
        super();
    }
    


    // window initialization
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        
        String url_rmi = "rmi://127.0.0.1/yechiel";
        try {
            //Username as a name for the server to be able to use this user's interface
            Naming.rebind(username,this);
            iServerUsers = (IServerUsers) Naming.lookup(url_rmi);
            iServerUsers.addUsers(username, board_name);
        } catch (Exception e) {}

        discussionLabel.setText("User: "+username+", Board: "+board_name);
        labelButton.setText("Group channel");
        
        // Pressing enter in the message field sending the message 
        messageInput.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.ENTER) {
                try {
                    sendMessageButtonOnAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        boardInitialization();       
    }
    
    


    // Initialize board
    public void boardInitialization() {                
        
        graphicsContext = canvas.getGraphicsContext2D();
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();
        
        graphicsContext.setStroke(color);
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(0, 0, canvasWidth, canvasHeight);                   
        
        // For the drawing board of new users who log in
        try {
                  iServerUsers.sendingBoard(username, board_name);
                } catch (RemoteException e){
                    e.printStackTrace();
                }
        
        resetButton.setTranslateX(10);
        resetButton.setOnAction(actionEvent -> {
            
            deleteBoard();
            
            // For update the other users
            try {
                    iServerUsers.sendDeleteBoard(username, board_name);
                }   catch (RemoteException e) {
                    e.printStackTrace();
                }
        });
     
        
        undoButton.setTranslateX(5);
        undoButton.setDisable(true);
        undoButton.setOnAction(actionEvent -> {
            
            undo();
            
            // For update the other users
            try {
                    iServerUsers.sendingUndo(username, board_name);
                }   catch (RemoteException e){
                    e.printStackTrace();
                }
        });
        
        
        redoButton.setTranslateX(5);
        redoButton.setDisable(true);
        redoButton.setOnAction(actionEvent -> {
            redo();

            // For update the other users
            try {
                  iServerUsers.sendingRedo(username, board_name);
                } catch (RemoteException e){
                    e.printStackTrace();
                }
        });
        

        ChoiceBox<String> colorChooser = new ChoiceBox<>(FXCollections.observableArrayList("Black", "Bleu", "Red", "Green", "Brown", "Orange"));
        colorChooser.getSelectionModel().selectFirst();

        colorChooser.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, old, newval) -> {
            Number idx = (Number) newval;
            color = switch (idx.intValue()) {
                case 0 -> Color.BLACK;
                case 1 -> Color.BLUE;
                case 2 -> Color.RED;
                case 3 -> Color.GREEN;
                case 4 -> Color.BROWN;
                case 5 -> Color.ORANGE;
                default -> Color.BLACK;
            };
        });

        
        ChoiceBox<String> typeChooser = new ChoiceBox<>(FXCollections.observableArrayList("Pen", "Line", "Filled Rectangle",  "Empty Rectangle", "Filled Oval", "Empty Oval", "Text", "Eraser"));
        typeChooser.getSelectionModel().selectFirst();

        typeChooser.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, old, newval) -> {
            Number idx = (Number) newval;
            type = switch (idx.intValue()) {
            case 0 -> "Pen";
            case 1 -> "Line";
            case 2 -> "Filled Rectangle";
            case 3 -> "Empty Rectangle";
            case 4 -> "Filled Oval";
            case 5 -> "Empty Oval";
            case 6 -> "Text";
            case 7 -> "Eraser";
            default -> "Pen";
        };
            
        });

        ChoiceBox<String> sizeChooser = new ChoiceBox<>(FXCollections.observableArrayList("1", "2", "3", "4", "5"));
        sizeChooser.getSelectionModel().selectFirst();
   
        sizeChooser.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, old, newval) -> {
            Number idx = (Number) newval;
            lineWidth = switch (idx.intValue()) {
                case 0 -> 1;
                case 1 -> 2;
                case 2 -> 3;
                case 3 -> 4;
                case 4 -> 5;
                default -> 1;
            };            
        });

        TextField text = new TextField("Text");
        
        buttonBox.getChildren().addAll(colorChooser, typeChooser, sizeChooser, resetButton);
        buttonBox2.getChildren().addAll(text, undoButton, redoButton);
        
        
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {                          

                click(event.getX(),event.getY(),lineWidth, String.valueOf(color), type, text.getText());           
                
                // For update the other users
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            iServerUsers.sendingClick(username,event.getX(),event.getY(),lineWidth, String.valueOf(color), type, text.getText(), board_name);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

                    
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                
                drag(event.getX(), event.getY(), lineWidth, String.valueOf(color), type);
                        
                // For update the other users
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            iServerUsers.sendingDrag(username,event.getX(),event.getY(),lineWidth, String.valueOf(color), type, board_name);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                        
                release(event.getX(), event.getY(), lineWidth, String.valueOf(color), type);

                // For update the other users
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            iServerUsers.sendingRelease(username,event.getX(),event.getY(),lineWidth, String.valueOf(color), type, board_name);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                   }
                });
            }
        });
    }     

    
    // Mouse click action
    public void click(double position_x, double position_y, double line_width, String color, String type, String text){              
        
        redoButton.setDisable(true); // Because there is a new operation so the redo operation does not work 
        while (!images_redo.empty()){
            images_redo.pop();
        }    
        
        // for action undo
        SnapshotParameters params = new SnapshotParameters();
        WritableImage write = new WritableImage((int)299, (int)390);
        params.setFill(Color.TRANSPARENT);
        current_image = canvas.snapshot(params, write); 
        images_undo.push(current_image);       
                
                
        Color this_color = Color.valueOf(color);
        graphicsContext.setLineWidth(line_width);
        graphicsContext.setStroke(this_color);
        x_position_click=position_x;
        y_position_click=position_y;

        graphicsContext.beginPath();
        graphicsContext.moveTo(position_x,position_y);
                
        if ("Text".equals(type)){
            graphicsContext.setLineWidth(1);
            graphicsContext.strokeText(text, position_x, position_y);
            
            // for action redo
            SnapshotParameters params2 = new SnapshotParameters();
            WritableImage write2 = new WritableImage((int)299, (int)390);
            params2.setFill(Color.TRANSPARENT);
            last_image = canvas.snapshot(params2, write2);
        }
                
        undoButton.setDisable(false); // Because there is a new operation so the undo operation is work
    }
    
    
    // Mouse drag action
    public void drag(double position_x, double position_y, double line_width, String color, String type){
                
        Color this_color = Color.valueOf(color);
        graphicsContext.setLineWidth(line_width);
        graphicsContext.setStroke(this_color);
        graphicsContext.lineTo(position_x, position_y);
                
        if ("Eraser".equals(type))
            graphicsContext.clearRect(position_x,  position_y, 5, 5);
                
        if ("Pen".equals(type))
            graphicsContext.stroke();
    }
    
    
    // Mouse release action
    public void release(double position_x, double position_y, double line_width, String color, String type){

        Color this_color = Color.valueOf(color);
        graphicsContext.setLineWidth(line_width);
        graphicsContext.setStroke(this_color);
        graphicsContext.setFill(this_color);

        double width = Math.abs(position_x - x_position_click);
        double height = Math.abs(position_y - y_position_click); 
                
        switch (type) {
            case "Line" -> graphicsContext.strokeLine(x_position_click, y_position_click, position_x, position_y);
            case "Filled Rectangle" -> graphicsContext.fillRect(Math.min(x_position_click, position_x), Math.min(y_position_click, position_y), width, height);
            case "Empty Rectangle" -> graphicsContext.strokeRect(Math.min(x_position_click, position_x), Math.min(y_position_click, position_y), width, height);
            case "Filled Oval" -> graphicsContext.fillOval(Math.min(x_position_click, position_x), Math.min(y_position_click, position_y), width, height);
            case "Empty Oval" -> graphicsContext.strokeOval(Math.min(x_position_click, position_x), Math.min(y_position_click, position_y), width, height);
        }        
        
        // for action redo
        SnapshotParameters params2 = new SnapshotParameters();
        WritableImage write2 = new WritableImage((int)299, (int)390);
        params2.setFill(Color.TRANSPARENT);
        last_image = canvas.snapshot(params2, write2);
                
   
    
    }
    
    
    // Delete board action    
    public void deleteBoard(){

        redoButton.setDisable(true); // Because there is a new operation so the redo operation does not work 
        while (!images_redo.empty()){
            images_redo.pop();
        }
        
        // for action undo
        SnapshotParameters params = new SnapshotParameters();
        WritableImage write = new WritableImage((int)299, (int)390);
        params.setFill(Color.TRANSPARENT);
        current_image = canvas.snapshot(params, write);  
        images_undo.push(current_image);
                    
        graphicsContext.clearRect(1, 1, graphicsContext.getCanvas().getWidth()-2, graphicsContext.getCanvas().getHeight()-2);
                
        // for action redo
        SnapshotParameters params2 = new SnapshotParameters();
        WritableImage write2 = new WritableImage((int)299, (int)390);
        params2.setFill(Color.TRANSPARENT);
        last_image = canvas.snapshot(params2, write2);
    }
        

    // undo action
    public void undo() {
                
        graphicsContext.clearRect(1, 1, graphicsContext.getCanvas().getWidth() - 2, graphicsContext.getCanvas().getHeight() - 2);
                
        current_image=images_undo.pop();
        images_redo.push(current_image);
                
        if (first_undo==true){
            current_image=images_undo.pop();
            images_redo.push(current_image);
        }
        first_undo=false;
        
        graphicsContext.drawImage(current_image, 0, 0);

        if (images_undo.empty())
            undoButton.setDisable(true);
                
        redoButton.setDisable(false);   
}
    
    
    // redo action
    public void redo() {

        graphicsContext.clearRect(1, 1, graphicsContext.getCanvas().getWidth() - 2, graphicsContext.getCanvas().getHeight() - 2);
                
        undoButton.setDisable(false);
                
        if ((images_redo.empty())){
            current_image=last_image;
            redoButton.setDisable(true);
            first_undo=false;
        }
        else{
            current_image=images_redo.pop();
            images_undo.push(current_image);
            
            if (first_undo==false){
                if ((images_redo.empty())){
                    current_image=last_image;
                    redoButton.setDisable(true);
                    first_undo=false;
                }
                else{
                    current_image=images_redo.pop();       
                    images_undo.push(current_image);
                    first_undo=true;
                }
            }
        }
                
        graphicsContext.drawImage(current_image, 0, 0);
    }

    
    // For disconnection
    public void disconnectButtonOnAction() throws IOException {
        iServerUsers.disconnectUser(username);

        Stage stage = (Stage) disconnectButton.getScene().getWindow();
        stage.close();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("log_in.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 350);
        Stage newStage = new Stage();
        newStage.initStyle(StageStyle.UNDECORATED);
        newStage.setScene(scene);
        newStage.show();
    }

   
    // To send message to other users 
    public void sendMessageButtonOnAction() throws RemoteException {
        if(messageInput.getText().isBlank() == false){
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_RIGHT);
            hbox.setPadding(new Insets(5,5,5,10));

            Text text = new Text(messageInput.getText());
            text.setFill(Color.color(0.934,0.945,0.996));

            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-color:rgb(239,242,255);"+" -fx-background-color:rgb(15,125,242);"+" -fx-background-radius: 7px 2px 2px 7px");
            textFlow.setPadding(new Insets(5,10,5,10));
            textFlow.setMaxWidth(150);

            hbox.getChildren().add(textFlow);

            hbox.heightProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldvalue, Object newValue) {
                    messageTextArea.setVvalue((Double)newValue );
                }
            });
            
            //Send message to all
            if(username_destination.equals("")){
                vBoxMessages.getChildren().add(hbox);
                messageTextArea.setContent(vBoxMessages);
                iServerUsers.sendMessage(username, board_name,  messageInput.getText());
            }
            //Send Message to this user
            else{
                for(int j = 0; j < users_channel.size() ; j++){
                    if(username_destination.equals(users_channel.get(j).username)){
                        users_channel.get(j).vBox.getChildren().add(hbox);
                        iServerUsers.sendMessage(username, board_name, messageInput.getText(), username_destination);
                    }
                }
            }

            messageInput.clear();
        }
    }

    

    // For to modify the list of connected users
    @Override
    public void editListOfUsers(ArrayList<String> list_users) throws RemoteException {
        if(list_users != null){
            for(int i = 0; i < list_users.size(); i++){
                VBox newVBox = new VBox();
                newVBox.setPadding(new Insets(10, 10, 10, 10));
                newVBox.setPrefWidth(278);
                boolean already_exists = false;

                if(!users_channel.isEmpty()){
                    for(int j = 0; j < users_channel.size(); j++){
                        if(list_users.get(i).equals(users_channel.get(j).username))
                            already_exists = true;
                    }
                    if(already_exists == false)
                        users_channel.add(new UsersDiscussions(list_users.get(i), newVBox));
                }   
                else
                    users_channel.add(new UsersDiscussions(list_users.get(i), newVBox));
            }

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    
                    listDisplayUsers.getItems().setAll(list_users);
                    listDisplayUsers.getItems().add(username);
                    listDisplayUsers.getItems();
                    listDisplayUsers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                      @Override
                      public void changed(ObservableValue<? extends String> observableValue, String s, String t) {

                            if((t == null)||(t.equals(username))){
                                username_destination = "";
                                labelButton.setText("Group channel");
                                vBoxMessages.setVisible(true);
                                messageTextArea.setContent(vBoxMessages);
                                for(int j = 0; j < users_channel.size() ; j++){
                                    users_channel.get(j).vBox.setVisible(false);
                                }
                            }else{
                                username_destination = t;
                                labelButton.setText("Private channel with "+username_destination);

                                vBoxMessages.setVisible(false);
                                for(int j = 0; j < users_channel.size() ; j++){
                                    if(username_destination.equals(users_channel.get(j).username)){
                                        messageTextArea.setContent(users_channel.get(j).vBox);
                                        users_channel.get(j).vBox.setVisible(true);
                                    }else{
                                        users_channel.get(j).vBox.setVisible(false);
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
    }



    // For to view message from other users
    @Override
    public void receiveMessage(String username, String message, String username_destination) throws RemoteException {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,5,5,10));

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(username_destination.equals("Group channel")){
                    Text textUsername = new Text(username+"\n");
                    Text textMessage = new Text(message);
                    textUsername.setFill(Color.web("002B5B"));
                    TextFlow textFlow = new TextFlow(textUsername,textMessage);
                    textFlow.setStyle("-fx-background-color:rgb(233,233,235);"+" -fx-background-radius: 2px 7px 7px 2px");
                    textFlow.setPadding(new Insets(5,10,5,10));
                    textFlow.setMaxWidth(150);

                    hbox.getChildren().add(textFlow);

                    hbox.heightProperty().addListener(new ChangeListener() {
                        @Override
                        public void changed(ObservableValue observable, Object oldvalue, Object newValue) {
                            messageTextArea.setVvalue((Double)newValue );
                        }
                    });

                    vBoxMessages.getChildren().add(hbox);
                }else{
                    Text textMessage = new Text(message);

                    TextFlow textFlow = new TextFlow(textMessage);
                    textFlow.setStyle("-fx-background-color:rgb(233,233,235);"+" -fx-background-radius: 2px 7px 7px 2px");
                    textFlow.setPadding(new Insets(5,10,5,10));
                    textFlow.setMaxWidth(150);

                    hbox.getChildren().add(textFlow);

                    hbox.heightProperty().addListener(new ChangeListener() {
                        @Override
                        public void changed(ObservableValue observable, Object oldvalue, Object newValue) {
                            messageTextArea.setVvalue((Double)newValue );
                        }
                    });

                    for(int j = 0; j < users_channel.size() ; j++){
                        if(username.equals(users_channel.get(j).username))
                            users_channel.get(j).vBox.getChildren().add(hbox);
                    }
                }
            }
        });
    }

        
        
    // To receive from other users the information of the mouse click
    @Override
    public void receiveClick(double position_x, double position_y, double line_width, String color, String type, String text) throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                click(position_x, position_y, line_width, color, type, text);    
            }
        });
    }

    
    
    // To receive from other users the information of the mouse drag
    @Override
    public void receiveDrag(double position_x, double position_y, double line_width, String color, String type) throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                drag(position_x, position_y, line_width, color, type);
            }
        });
         
    }

    

    // To receive from other users the information of the mouse release
    @Override
    public void receiveRelease(double position_x, double position_y, double line_width, String color, String shape) throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                release(position_x, position_y, line_width, color, shape); 
            }
        });
        
    }
    
    
    
    // Receive delete board action
    @Override
    public void receiveDeleteBoard() throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                deleteBoard();
            }
        });
    }


    // Receive undo action
    @Override
    public void receiveUndo() throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                undo();
            }
        });
    }
    
    
    // Receive redo action
    @Override
    public void receiveRedo() throws RemoteException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                redo();
            }
        });
    }

 
    // Send file to other users
    public void chooseFileSendButtonOnAction(){
        Stage stage = new Stage();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                ArrayList<Integer> j;
                try (FileInputStream in = new FileInputStream(selectedFile)) {
                    j = new ArrayList<>();
                    int i=0;
                    while((i=in.read()) != -1) {
                        j.add(i);
                    }
                }
                iServerUsers.sendFile(username,j, selectedFile.getName(),username_destination);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


    // Receive file from other users
    @Override
    public void receiveFile(String username_source, ArrayList<Integer> j, String file_name, String username_destination) throws RemoteException{

        final ContextMenu contextMenu = new ContextMenu();
        Download download = new Download();
        download.downloadFile(contextMenu, j, file_name);

        HBox hbox = new HBox();
        hbox.setPadding(new Insets(5,5,5,10));

        hbox.heightProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldvalue, Object newValue) {
                messageTextArea.setVvalue((Double)newValue );
            }
        });

        hbox.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isSecondaryButtonDown()) {
                    contextMenu.show(hbox, event.getScreenX(), event.getScreenY());
                }
            }
        });

        Text textFile = new Text(file_name);

        if(username.equals(username_source)){
            hbox.setAlignment(Pos.CENTER_RIGHT);
            TextFlow textFlow = new TextFlow(textFile);
            textFlow.setStyle("-fx-color:rgb(239,242,255);"+" -fx-background-color:rgb(15,125,242);"+" -fx-background-radius: 7px 2px 2px 7px");
            textFlow.setPadding(new Insets(5,10,5,10));
            textFlow.setMaxWidth(150);
            hbox.getChildren().add(textFlow);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(username_destination.equals("")){
                        vBoxMessages.getChildren().add(hbox);
                    }else{
                        for(int j = 0; j < users_channel.size() ; j++){
                            if(username_destination.equals(users_channel.get(j).username)){
                                users_channel.get(j).vBox.getChildren().add(hbox);
                            }
                        }
                    }
                }
            });
        }else{
            hbox.setAlignment(Pos.CENTER_LEFT);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(username_destination.equals("")){
                        Text textNomUtilisateur = new Text(username_source+"\n");
                        textNomUtilisateur.setFill(Color.web("002B5B"));

                        TextFlow textFlow = new TextFlow(textNomUtilisateur,textFile);
                        textFlow.setStyle("-fx-background-color:rgb(233,233,235);"+" -fx-background-radius: 2px 7px 7px 2px");
                        textFlow.setPadding(new Insets(5,10,5,10));
                        textFlow.setMaxWidth(150);

                        hbox.getChildren().add(textFlow);

                        vBoxMessages.getChildren().add(hbox);
                    }else{
                        TextFlow textFlow = new TextFlow(textFile);
                        textFlow.setStyle("-fx-background-color:rgb(233,233,235);"+" -fx-background-radius: 2px 7px 7px 2px");
                        textFlow.setPadding(new Insets(5,10,5,10));
                        textFlow.setMaxWidth(150);

                        hbox.getChildren().add(textFlow);

                        for(int j = 0; j < users_channel.size() ; j++){
                            if(username_source.equals(users_channel.get(j).username)){
                                users_channel.get(j).vBox.getChildren().add(hbox);
                            }
                        }
                    }
                }
            });
        }
    }
}