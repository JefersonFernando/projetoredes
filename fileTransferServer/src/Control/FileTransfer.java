/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.event.EventHandler;

/**
 *
 * @author jefer
 */
public class FileTransfer extends Application{
    
    private static Stage stage;
    private static Scene mainMenuScene;
    private static Scene updaterScene;
    
    @Override
    public void start(Stage _stage) throws Exception {
        stage = _stage;
        Parent fxmlMain = FXMLLoader.load(getClass().getResource("GUI.fxml"));
        mainMenuScene = new Scene(fxmlMain);
        
        stage.setScene(mainMenuScene);
        stage.setResizable(false);
        stage.show();
        
        stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
        
            @Override
            public void handle(WindowEvent we){
                Client client = Client.getInstance();
                Server server = Server.getInstance();
                server.stopServer();
                client.stopClient();
            }
        });
    }
    
    public static void main(String[] args){
        launch(args);
    }
    
}
