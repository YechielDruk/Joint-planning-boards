package server;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Main {
    public static void main(String[] args) throws RemoteException, MalformedURLException, AlreadyBoundException {

        ServerUsers serverUsers = new ServerUsers();
        ServerRegistration serverRegistration = new ServerRegistration();

        LocateRegistry.createRegistry(1099);
        Naming.bind("yechiel",serverUsers);
        Naming.bind("yechiel2",serverRegistration);

        System.out.println("Server is ready");
    }
}
