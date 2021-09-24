/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.io.File;
import java.util.Arrays;

/**
 *
 * @author jefer
 */
public final class StartCommand extends BasicCommand{
    
    private static StartCommand instance = null;
    private static final OptionCommands command = OptionCommands.START;
    private ReceiveCommand receiveCommand = null;
    private SendCommand sendCommand = null;
    private File file = null;
    
    private StartCommand(){
        this.server = Server.getInstance();
        this.client = Client.getInstance();
        this.receiveCommand = ReceiveCommand.getInstance();
        this.sendCommand = SendCommand.getInstance();
    }
    
    public static StartCommand getInstance(){
        if(instance == null){
            instance = new StartCommand();
        }
        return instance;
    }
    
    @Override
    void setInterfaceController(GUIController controller){
        interfaceController = controller;
    }
    
    public void feed(String text){
        if(text != null){
            file = new File(text);
            if(file != null){
                byte[] msg = new byte[1024];
                
                msg[0] = (byte)command.getValue();
                
                msg[1] = 0x00;
                
                byte[] name = file.getName().getBytes();
                msg[2] = (byte)name.length;
                
                int index = 3;
                
                for(int i = 0; i<name.length; i++){
                    msg[index] = name[i];
                    index++;
                }
                
                msg[index] = 0x08;
                index++;
                 
                long fileSize = file.length();
                for(int i = 7; i >= 0; i--){
                    msg[index + i] = (byte)(fileSize & 0xFF);
                    fileSize >>= 8;
                }
                index+=8;
                byte[] finalMsg = Arrays.copyOfRange(msg, 0, index);
                
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
    
    @Override
    public void process(byte[] bytes){
        int err = bytes[0];
        
        if(err == 0){
            int sizeName = bytes[1];
            byte[] fileName = new byte[sizeName];
            int index = 2;
            
            for(int i = 0; i < sizeName; i++){
                fileName[i] = bytes[index];
                index++;
            }
            
            int sizeLen = bytes[index];
            index++;
            
            long sizeFile = 0;
            
            for(int i = sizeLen - 1; i >= 0; i--){
                sizeFile |= ((bytes[index] & 0xFF) << 8*i);
                index++;
            }
            
            interfaceController.transferRequest(new String(fileName), sizeFile);
            
        }
        
    }
    
}
