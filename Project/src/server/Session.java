package server;

import java.util.LinkedList;
import java.util.Queue;
import users.IUsers;


public class Session {
    public String username;
    public String board_name;
    public IUsers iUsers;
    public Queue<Object[]> operations = new LinkedList<>();

    
    public Session(String username, String board_name, IUsers iUsers){
        this.username = username;
        this.board_name = board_name;
        this.iUsers = iUsers;
    }
}
