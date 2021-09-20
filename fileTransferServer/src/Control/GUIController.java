/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import java.io.File;
import java.util.regex.Pattern;
import javafx.stage.FileChooser;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

/**
 *
 * @author jefer
 */
public class GUIController {
    
    public Button onOffButton;
    public Button sendButton;
    public Button acceptButton;
    public Button rejectButton;
    
    public Circle conectionStatus;
    public Circle sendError;
    public Circle receiveError;
    
    public ChoiceBox selector;
    
    public ProgressIndicator sendIndicator;
    public ProgressIndicator receiveIndicator;
    
    public TextField ipField;
    public TextField portField;
    public TextField receiveRequest;
    
    private Server server = null;
    private Client client = null;
    private StartCommand startCommand = null;
    
    public void initialize(){
        selector.getItems().add("Servidor");
        selector.getItems().add("Cliente");
        disconnectedStatus();
        server = Server.getInstance();
        client = Client.getInstance();
        startCommand = StartCommand.getInstance();
        server.setInterfaceController(this);
        client.setInterfaceController(this);
        startCommand.setInterfaceController(this);
    }
    
    public void disconnectedStatus(){
        sendButton.setDisable(true);
        
        acceptButton.setDisable(true);
        acceptButton.setOpacity(0);
        
        rejectButton.setDisable(true);
        rejectButton.setOpacity(0);
        
        sendError.setOpacity(0);
        receiveError.setOpacity(0);
        
        selector.setDisable(false);
        
        sendIndicator.setOpacity(0);
        receiveIndicator.setOpacity(0);
        
        receiveRequest.setDisable(true);
        receiveRequest.setOpacity(0);
        
        conectionStatus.setFill(Color.RED);
    }
    
    public void OnlineStatus(){
        sendButton.setDisable(true);
        
        acceptButton.setDisable(true);
        acceptButton.setOpacity(0);
        
        rejectButton.setDisable(true);
        rejectButton.setOpacity(0);
        
        sendError.setOpacity(0);
        receiveError.setOpacity(0);
        
        selector.setDisable(true);
        
        sendIndicator.setOpacity(0);
        receiveIndicator.setOpacity(0);
        
        ipField.setDisable(true);
        portField.setDisable(true);
        
        receiveRequest.setDisable(true);
        receiveRequest.setOpacity(0);
        
        conectionStatus.setFill(Color.BLUE);
    }
    
    public void connectedStatus(){
        sendButton.setDisable(false);
        
        acceptButton.setDisable(true);
        acceptButton.setOpacity(0);
        
        rejectButton.setDisable(true);
        rejectButton.setOpacity(0);
        
        sendError.setOpacity(0);
        receiveError.setOpacity(0);
        
        selector.setDisable(true);
        
        sendIndicator.setOpacity(0);
        receiveIndicator.setOpacity(0);
        
        ipField.setDisable(true);
        portField.setDisable(true);
        
        receiveRequest.setDisable(true);
        receiveRequest.setOpacity(0);
        
        conectionStatus.setFill(Color.GREEN);
    }
    
    public void conectionChanged(){
        if(selector.getValue() == "Servidor"){
            ipField.clear();
            ipField.setDisable(true);
            portField.setDisable(false);
        }else if(selector.getValue() == "Cliente"){
            ipField.setDisable(false);
            portField.setDisable(false);
        }
    }
    
    private boolean checkIp(String ip){
        String regex = "[0-9][0-9.]*[0-9]";
        return Pattern.matches(regex, ip);
    }
    
    public String getIp(){
        String ip = ipField.getText();
        if(ip != null && checkIp(ip)){
            return ip;
        }
        return "-";
    }
    
    private boolean checkPort(String port){
        String regex = "[0-9]+";
        return Pattern.matches(regex, port);
    }
    
    public int getPort(){
        String port = portField.getText();
        if(port != null && checkPort(port)){
            return Integer.parseInt(port);            
        }
        return -1;
    }
    
    public void handeOnOffButton(){
        if(server.isOn() || client.isOn()){
            connectionOff();
        }
        else {
            OnlineStatus();
            if(selector.getValue() == "Servidor"){
                new Thread(server).start();
            }else if(selector.getValue() == "Cliente"){
                new Thread(client).start();
            }
        }
    }
    
    public void connectionOff(){
        server.stopServer();
        client.stopClient();
        disconnectedStatus();
    }
    
    public void handleSendButton(){
        StartCommand start = StartCommand.getInstance();
        
        FileChooser fc = new FileChooser();
        
        File file = fc.showOpenDialog(null);
        if(file != null){
            start.feed(file.getAbsolutePath());
        }
    }
    
    public void transferRequest(String fileName, long fileSize){
        receiveRequest.setOpacity(1);
        receiveRequest.clear();
        receiveRequest.appendText("Deseja receber " + fileName + "?" + fileSize + "Bytes");
        
        acceptButton.setOpacity(1);
        acceptButton.setDisable(false);
        
        rejectButton.setOpacity(1);
        rejectButton.setDisable(false);
    }
}
