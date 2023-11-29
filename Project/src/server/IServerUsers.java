package server;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;


public interface IServerUsers extends Remote{
    public String[] logIn(String username, String password) throws RemoteException, SQLException, MalformedURLException, NotBoundException;
    public void addUsers(String username, String board_name) throws RemoteException, MalformedURLException, NotBoundException;
    public void disconnectUser(String username) throws RemoteException;
    public void sendMessage(String username, String board_name, String message) throws RemoteException;
    public void sendMessage(String username, String board_name, String message, String username_destination) throws RemoteException;
    public void sendingClick(String username, double position_x, double position_y, double line_width, String color, String type, String Text, String board_name) throws RemoteException;
    public void sendingDrag(String username, double position_x, double position_y, double line_width, String color, String type, String board_name) throws RemoteException;
    public void sendingRelease(String username, double position_x, double position_y, double line_width, String color, String type, String board_name) throws RemoteException;
    public void sendDeleteBoard(String username, String board_name) throws RemoteException;
    public void sendingUndo(String username, String board_name) throws RemoteException;
    public void sendingRedo(String username, String board_name) throws RemoteException;
    public void sendFile(String username_source, ArrayList<Integer> j, String file_name, String username_destination) throws RemoteException;
    public void sendingBoard(String username, String board_name) throws RemoteException;
}
