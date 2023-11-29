package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;

public interface IServerRegistration extends Remote {
    public ArrayList<String> displayList() throws RemoteException, SQLException;
    public String[] addUserToTheDatabase(String last_name, String first_name, String username, String password, String board_name) throws RemoteException, SQLException;
    public String[] addBoardToTheDatabase(String board_name) throws RemoteException, SQLException;
}
