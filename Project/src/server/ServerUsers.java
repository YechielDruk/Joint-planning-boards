package server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import users.IUsers;



public class ServerUsers extends UnicastRemoteObject implements IServerUsers{

    private static ArrayList<Session> session = new ArrayList<Session>();
    
    final String DB_URL = "jdbc:mysql://localhost:3306/mysql";
    final String USER = "root";
    final String PASSWORD = "3683683680";

 
    protected ServerUsers() throws RemoteException {
        super();
    }



    // Validates user information
    @Override
    public String[] logIn(String username, String password) throws RemoteException, SQLException{
        boolean user_already_connected = false;
        
        String hashed_user_password = PasswordHashing.passwordHashing(password);
        String db_password = "";        
        String board_name = "";
        String[] message = new String[2];
     
       
        // Check if the user is logged in
        for(int i = 0 ; i < session.size(); i++){
            if(session.get(i).username.equals(username)){
                user_already_connected = true;
                break;
            }
        }

        if(!user_already_connected){
            
            // Retrieve the password of this user from the db
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            String strSelect = "Select * from registration where username= ?";

            PreparedStatement st = conn.prepareStatement(strSelect);
            st.setString(1,username);

            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                db_password = rs.getString(4);
                board_name = rs.getString(5);
            }

            if(hashed_user_password.equals(db_password)){
                message[0] = "success";
                message[1] = board_name;
            }else{
                message[0] = "error";
                message[1] = "Username or password is incorrect";
            }
        }else{
            message[0] = "error";
            message[1] = "You are already connected!";
        }

        return message;
    }

    

    // Return list of all users on the same board
    public ArrayList<String> ListOfUsers(String username, String board_name){
        ArrayList<String> list_of_users = new ArrayList<>();
        
        for (int i = 0 ; i < session.size() ; i++){
            if((!session.get(i).username.equals(username)) && (session.get(i).board_name.equals(board_name))){
                list_of_users.add(session.get(i).username);
            }
        }
        return list_of_users;
    }

  
    
    // Add the user in the session with his interface and update the list of users
    @Override
    public void addUsers(String username, String board_name) throws RemoteException, MalformedURLException, NotBoundException {
       
        //Add the user to the session
        IUsers user = (IUsers) Naming.lookup("rmi://127.0.0.1/" + username);
        session.add(new Session(username, board_name, user));

        //Update the list of users
        for(int i = 0 ; i < session.size() ; i++){
            session.get(i).iUsers.editListOfUsers(ListOfUsers(session.get(i).username, session.get(i).board_name));
        }
    }

    

    // Removing the user from the session
    @Override
    public void disconnectUser(String username) throws RemoteException {

        for (int i = 0 ; i<session.size();i++){
            if(session.get(i).username.equals(username)){
                session.remove(i);
                break;
            }
        }
        
        //Update the list of users
        for(int i = 0 ; i < session.size() ; i++){
            session.get(i).iUsers.editListOfUsers(ListOfUsers(session.get(i).username, session.get(i).board_name));
        }
    }

    

    // Send to other users the information of the mouse click
    @Override
    public void sendingClick(String username, double position_x, double position_y, double line_width, String color, String type, String Text, String board_name) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if((!session.get(i).username.equals(username)) && (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveClick(position_x, position_y, line_width, color, type, Text);
            }
        
            // For drawing board of new users who log in
            Object[] operation={1, position_x, position_y, line_width, color, type, Text};
            if(session.get(i).board_name.equals(board_name))
            session.get(i).operations.add(operation);
        }
    }
    
    

    // Send to other users the information of the mouse drag
    @Override
    public void sendingDrag(String username, double position_x, double position_y, double line_width, String color, String type, String board_name) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if((!session.get(i).username.equals(username)) && (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveDrag(position_x, position_y, line_width, color, type);
            }
        
            // For drawing board of new users who log in
            Object[] operation={2, position_x, position_y, line_width, color, type};
            if(session.get(i).board_name.equals(board_name))
            session.get(i).operations.add(operation);
        }
    }

    

    // Send to other users the information of the mouse release
    @Override
    public void sendingRelease(String username, double position_x, double position_y, double line_width, String color, String type, String board_name) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).username.equals(username) && (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveRelease(position_x, position_y, line_width, color, type);
            }
            
            // For drawing board of new users who log in
            Object[] operation={3, position_x, position_y, line_width, color, type};
            if(session.get(i).board_name.equals(board_name))
            session.get(i).operations.add(operation);
        }
    }       
    
    

    // Send to other users the information of the delete board Drawings
    @Override
    public void sendDeleteBoard(String username, String board_name) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).username.equals(username) && (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveDeleteBoard();
            }
            
            // For drawing board of new users who log in
            Object[] operation={4};
            if(session.get(i).board_name.equals(board_name))
            session.get(i).operations.add(operation);
        }
    }
    
    

    // Send to other users the information of the execution of an undo operation
    @Override
    public void sendingUndo(String username, String board_name) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).username.equals(username) && (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveUndo();
            }
            
            // For drawing board of new users who log in
            Object[] operation={5};
            if(session.get(i).board_name.equals(board_name))
            session.get(i).operations.add(operation);
        }
    }
    
    

    // Send to other users the information of the execution of an redo operation
    @Override
    public void sendingRedo(String username, String board_name) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if(!session.get(i).username.equals(username) && (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveRedo();
            }
            
            // For drawing board of new users who log in
            Object[] operation={6};
            if(session.get(i).board_name.equals(board_name))
            session.get(i).operations.add(operation);
        }
    }

    
    
    // For the drawing board of new users who log in
    @Override
    public void sendingBoard(String username, String board_name)throws RemoteException {
        
        Queue<Object[]> current_operations = new LinkedList<>();

        for(int i = 0 ; i < session.size() ; i++){

            if((!session.get(i).operations.isEmpty())&& (session.get(i).board_name.equals(board_name))){
                current_operations=session.get(i).operations;
                break;                
            } 
        }
        
        for(int i = 0 ; i < session.size() ; i++){
            if(session.get(i).username.equals(username)){
                for(int j = 0 ; j < current_operations.size() ; j++){
                        
                    Object[] operation=current_operations.remove();   
                    current_operations.add(operation);
                    session.get(i).operations.add(operation);

                    if ((int)operation[0]==1)
                        session.get(i).iUsers.receiveClick((double)operation[1],(double) operation[2],(double) operation[3], (String)operation[4], (String)operation[5], (String)operation[6]);
                    if ((int)operation[0]==2)
                        session.get(i).iUsers.receiveDrag((double)operation[1],(double) operation[2],(double) operation[3], (String)operation[4], (String)operation[5]); 
                    if ((int)operation[0]==3)
                        session.get(i).iUsers.receiveRelease((double)operation[1],(double) operation[2],(double) operation[3], (String)operation[4], (String)operation[5]); 
                    if ((int)operation[0]==4)
                        session.get(i).iUsers.receiveDeleteBoard();
                    if ((int)operation[0]==5)
                        session.get(i).iUsers.receiveUndo();
                    if ((int)operation[0]==6)
                        session.get(i).iUsers.receiveRedo();
                }    
            }
        }
    }
    
    
    
    // send message from one user to all users in his group
    @Override
    public void sendMessage(String username, String board_name, String message) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if((!session.get(i).username.equals(username))&& (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveMessage(username,message,"Group channel");
            }
        }
    }

    

    // send message from one user to another in his group
    @Override
    public void sendMessage(String username, String board_name, String message, String destination_username) throws RemoteException {
        for(int i = 0 ; i < session.size() ; i++){
            if((session.get(i).username.equals(destination_username))&& (session.get(i).board_name.equals(board_name))){
                session.get(i).iUsers.receiveMessage(username, message, destination_username);
            }
        }
    }
    
    
    
    // Send files to other users
    @Override
    public void sendFile(String username_source, ArrayList<Integer> j, String file_name, String username_destination) throws RemoteException {
        
        // send message from one user to all users in his group
        if(username_destination.equals("")){
            for(int i = 0 ; i < session.size() ; i++){
                session.get(i).iUsers.receiveFile(username_source, j, file_name, username_destination);
            }
        }
        
        // send file from one user to another in his group
        else{
            for(int i = 0 ; i < session.size() ; i++){
                if(username_destination.equals(session.get(i).username) || username_source.equals(session.get(i).username)){
                    session.get(i).iUsers.receiveFile(username_source, j, file_name, username_destination);
                }
            }
        }
    }    
}
