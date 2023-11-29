package users;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface IUsers extends Remote {
    public void editListOfUsers(ArrayList<String> list) throws RemoteException;
    public void receiveMessage(String username, String message, String username_destination) throws RemoteException;
    public void receiveClick(double position_x, double position_y, double line_width, String color, String type, String text) throws RemoteException;
    public void receiveDrag(double position_x, double position_y, double line_width, String color, String type) throws RemoteException;
    public void receiveRelease(double position_x, double position_y, double line_width, String color, String type) throws RemoteException;
    public void receiveDeleteBoard() throws RemoteException;
    public void receiveUndo() throws RemoteException;
    public void receiveRedo() throws RemoteException;
    public void receiveFile(String username_source, ArrayList<Integer> j, String file_name, String username_destination) throws RemoteException;
}
