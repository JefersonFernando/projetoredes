/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Button;
import java.io.File;
import java.util.regex.Pattern;
import javafx.stage.FileChooser;
import javax.swing.JFileChooser;

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
    private ReceiveCommand receiveCommand = null;
    private SendCommand sendCommand = null;
    
    private String fileName = null;
    private long fileSize = 0;
    
    public void initialize(){
        selector.getItems().add("Servidor");
        selector.getItems().add("Cliente");
        disconnectedStatus();
        server = Server.getInstance();
        client = Client.getInstance();
        startCommand = StartCommand.getInstance();
        receiveCommand = ReceiveCommand.getInstance();
        sendCommand = SendCommand.getInstance();
        server.setInterfaceController(this);
        client.setInterfaceController(this);
        startCommand.setInterfaceController(this);
        receiveCommand.setInterfaceController(this);
        sendCommand.setInterfaceController(this);
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
        if(ip != null){
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
        
        sendIndicator.setOpacity(1);
        sendIndicator.setProgress(0);
        
        File file = fc.showOpenDialog(null);
        if(file != null){
            sendCommand.setFileConfig(file.getAbsolutePath(), file.length());
            start.feed(file.getAbsolutePath());
        }
    }
    
    public void handleAcceptButton(){
        receiveIndicator.setOpacity(1);
        receiveIndicator.setProgress(0);
        byte err = 0x00;
        
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(fileName));
        fc.showSaveDialog(null);
        receiveCommand.setFileConfig(fc.getSelectedFile(), fileSize);
        receiveCommand.feed(err);
    }
    
    public void handleRejectButton(){
        byte err = (byte) -1;
        receiveCommand.feed(err);
        
        receiveCommand.interruptFileTransfer();
        
        this.fileName = null;
        this.fileSize = 0;
        
        connectedStatus();
    }
    
    public void transferRequest(String fileName, long fileSize){
        String sizeType;
        
        this.fileName = fileName;
        this.fileSize = fileSize;
        
        if(fileSize < 1024){
            sizeType = "Bytes";
        }else if(fileSize < Math.pow(1024, 2)){
            fileSize /= 1024;
            sizeType = "KB";
        }else if(fileSize < Math.pow(1024, 3)){
            fileSize /= Math.pow(1024, 2);
            sizeType = "MB";
        }else{
            fileSize /= Math.pow(1024, 3);
            sizeType = "GB";
        }
        
        receiveRequest.setOpacity(1);
        receiveRequest.clear();
        receiveRequest.appendText("Deseja receber " + fileName +  "? " + fileSize + sizeType);
        
        acceptButton.setOpacity(1);
        acceptButton.setDisable(false);
        
        rejectButton.setOpacity(1);
        rejectButton.setDisable(false);
    }
}
