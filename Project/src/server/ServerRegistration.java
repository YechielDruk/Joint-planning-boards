package server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;



public class ServerRegistration extends UnicastRemoteObject implements IServerRegistration, Serializable{ 
    
    final String DB_URL = "jdbc:mysql://localhost:3306/mysql";
    final String USER = "root";
    final String PASSWORD = "3683683680";
    
    protected ServerRegistration() throws RemoteException {
        super();
    }
    

    
    // Retrieve the list of boards from the database
    @Override
    public ArrayList<String> displayList() throws RemoteException, SQLException {
        ArrayList<String> listSend = new ArrayList<>();
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);

        String strSelect = "Select * from boards";
            
        PreparedStatement st = conn.prepareStatement(strSelect);

        ResultSet rs = st.executeQuery();
            
        while (rs.next()) {
            listSend.add(rs.getString("board_name"));
        }
        
        return listSend;
    }

    
    
    // Add a user to the database
    @Override
    public String[] addUserToTheDatabase(String last_name, String first_name, String username, String password, String board_name) throws RemoteException, SQLException {
        String[] message_to_display = new String[2];

        Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement();

        // Check if the username is available
        String selectQuery = "Select * from registration where username= ?";
        PreparedStatement st = conn.prepareStatement(selectQuery);
        st.setString(1,username);

        ResultSet rs = st.executeQuery();
        boolean exists = false;
        while (rs.next()) {
            exists = true;
        }

        if(exists){
            message_to_display[0] = "error";
            message_to_display[1] = "This username is not available";
        }else{
            String insertQuery = "INSERT INTO registration VALUES ('"+username+"','"+last_name+"','"+first_name+"','"+PasswordHashing.passwordHashing(password)+"','"+board_name+"')";
            stmt.executeUpdate(insertQuery);

            message_to_display[0] = "success";
            message_to_display[1] = "The user is successfully created";
        }

        return message_to_display;
    }

    
    
    // Add a board to the database
    @Override
    public String[] addBoardToTheDatabase(String board_name) throws RemoteException, SQLException {
        String[] message_to_display = new String[2];

        Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        Statement stmt = conn.createStatement();

        // Check if the board name is available
        String selectQuery = "Select * from boards where board_name= ?";
        PreparedStatement st = conn.prepareStatement(selectQuery);
        st.setString(1,board_name);

        ResultSet rs = st.executeQuery();
        boolean exists = false;
        while (rs.next()) {
            exists = true;
        }
        
        if(exists){
            message_to_display[0] = "error";
            message_to_display[1] = "This board name is not available";
        }else{
            String insertQuery = "INSERT INTO boards VALUES ('"+board_name+"')";
            stmt.executeUpdate(insertQuery);

            message_to_display[0] = "success";
            message_to_display[1] = "The board was successfully created";
        }

        return message_to_display;
    }
    
}
