/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.io.*;
import java.nio.file.*;

/**
 *
 * @author jefer
 */
public final class ReceiveCommand extends BasicCommand{
    private static ReceiveCommand instance = null;
    private File file = null;
    private long fileSize = 0;
    private long receivedData = 0;
    private boolean receivingData = false;
    
    private static final OptionCommands command = OptionCommands.SEND;
    
    private ReceiveCommand(){
        client = Client.getInstance();
        server = Server.getInstance();
    }
    
    public static ReceiveCommand getInstance(){
        if(instance == null){
            instance = new ReceiveCommand();
        }
        return instance;
    }
        
    public void setFileConfig(File file, long FileSize){
        this.file = file;
        this.fileSize = FileSize;
        this.receivedData = 0;
        this.receivingData = true;
        
        try{
            Files.deleteIfExists(file.toPath());
            file.createNewFile();
        }catch(IOException e){
            interruptFileTransfer();
        }
    }
    
    public void interruptFileTransfer(){
        if(file != null){
            file.delete();
        }
        this.file = null;
        this.fileSize = 0;
        this.receivedData = 0;
        this.receivingData = false;
        interfaceController.connectedStatus();
    }
    
    public boolean isReceivingData(){
        return receivingData;
    }
    
    public void setInterfaceController(GUIController controller){
        this.interfaceController = controller;
    }
    
    void process(byte[] bytes){
        int err = 0;
        int currentReceived;
        err = bytes[0];
        if(err == 0 && file != null){
            if(file.canWrite()){
                currentReceived = 0;
                currentReceived |= ((bytes[1] & 0xFF) << 8);
                currentReceived |= (bytes[2] & 0xFF);
                receivedData += currentReceived;
                interfaceController.receiveIndicator.setProgress((double)receivedData/fileSize);
                byte[] data = new byte[currentReceived];
                for(int i = 0; i < currentReceived; i++){
                    data[i] = bytes[i+3];
                }
                try{
                    Files.write(file.toPath(), data, StandardOpenOption.APPEND);
                } catch(IOException e){
                    interruptFileTransfer();
                    err = -1;
                }
            }else{
                err = -1;
            }
            feed((byte)err);
        }else if(err == 1){
            file = null;
            fileSize = 0;
            receivedData = 0;
            receivingData = false;
            interfaceController.connectedStatus();
            err = 0;
        }else{
            interruptFileTransfer();
        }
    }
    
    void feed(byte text){
        byte[] msg = new byte[2];
        msg[0] = (byte)command.getValue();
        msg[1] = text;
        if(server.isConnected()){
            server.send(msg);
        }else if(client.isConnected()){
            client.send(msg);
        }else{
            server.interruptServer();
        }
    }
    
}
