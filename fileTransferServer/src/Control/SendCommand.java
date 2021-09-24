/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author jefer
 */
public final class SendCommand extends BasicCommand{
    private static SendCommand instance = null;
    private File file;
    private FileInputStream fileReader;
    private boolean sendingData = false;
    private long fileSize = 0;
    private long sentData = 0;
    
    private static final OptionCommands command = OptionCommands.RECEIVE;
    
    private SendCommand(){
        server = Server.getInstance();
        client = Client.getInstance();
    }
    
    public static SendCommand getInstance(){
        if(instance == null){
            instance = new SendCommand();
        }
        return instance;
    }
    
    @Override
    public void setInterfaceController(GUIController controller){
        interfaceController = controller;
    }
    
    public void interruptFileTransfer(){
        this.file = null;
        sendingData = false;
        fileSize = 0;
        sentData = 0;
        interfaceController.connectedStatus();
    }
    
    public void setFileConfig(String file, long fileSize){
        try{
            this.file = new File(file);
            fileReader = new FileInputStream(file);
            this.fileSize = file.length();
            sentData = 0;
            sendingData = true;
        }catch(FileNotFoundException ex){
            interruptFileTransfer();
        }
    }
    
    @Override
    public void process(byte[] bytes){
        int  err = bytes[0];
        int content;
        int index;
        if(err == 0){
            byte[] msg = new byte[1024];
            msg[0] = (byte) command.getValue();
            msg[1] = 0x00;
            
            for(index = 4; index < msg.length; index++){
                try{
                    if(fileReader.available() != 0){
                        try{
                            content = fileReader.read();
                            sentData++;
                            if(content != -1){
                                msg[index] = (byte) content;
                            }
                        } catch(IllegalStateException e){
                            err = -1;
                            break;
                        }
                    }else {
                        break;
                    }
                }catch (IOException e){
                    
                }
            }
            
            if(err != 0){
                msg[1] = (byte)err;
            }else if(index == 4){
                msg[1] = 0x01;
            }
            
            index = index - 4;
            
            msg[2] = (byte)((index >> 8) & 0xFF);
            msg[3] = (byte)(index & 0xFF);
            
            interfaceController.sendIndicator.setProgress((double)sentData/fileSize);
            
            byte[] finalMsg = Arrays.copyOfRange(msg, 0, index + 4);
            
            if(server.isConnected()){
                server.send(finalMsg);
            }else if(client.isConnected()){
                client.send(finalMsg);
            }else{
                server.interruptServer();
            }
             
        }
        
    } 
}
