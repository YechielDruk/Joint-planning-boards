package users;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;



public class Download {
    
    public void downloadFile(ContextMenu contextMenu, ArrayList<Integer> j, String file_name){
        MenuItem download = new MenuItem("Download");
        contextMenu.getItems().addAll(download);
        
        download.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileOutputStream fileOutputStream;
                String directory_chosen;
                String divider = "\\";

                try{
                    //Choose the destination directory
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    Stage stage = new Stage();
                    File selectedFile = directoryChooser.showDialog(stage);
                    directory_chosen = selectedFile.getAbsolutePath();

                    fileOutputStream = new FileOutputStream( directory_chosen + divider + file_name);
                    
                    for (int i = 0; i<j.size(); i++) {
                        int c = j.get(i);
                        fileOutputStream.write((byte)c);
                    }
                    
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }catch (Exception e){}
            }
        });
    }
    
}
