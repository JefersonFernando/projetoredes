/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.net.*;
import java.util.Map;
import java.io.*;

/**
 *
 * @author jefer
 */
public final class ServerReceiver implements Runnable{
    
    private Server server = null;
    private static ServerReceiver instance = null;
    private volatile boolean exit = false;
    private Socket socket = null;
    private InputStream input = null;
    
    private Map<OptionCommands, Commands> commands;
    
    private ServerReceiver(){
    }
    
    public void setServer(Server server){
        this.server = server;
    }
    
    public static ServerReceiver getInstance(){
        if(instance == null){
            instance = new ServerReceiver();
        }
        return instance;
    }
    
    public void setSocket(Socket socket){
        this.socket = socket;
        if(socket != null){
            try{
                input = socket.getInputStream();
            } catch (IOException e){
                exit = true;
                server.interruptServer();
            }
        }
    }
    
    @Override
    public void run(){
        exit = false;
        byte[] data2 = new byte[65536];
        byte[] data = new byte[65536];
        int size, crc;
        
        Commands manager = Commands.getInstance();
        long crcValue;
        int receivedBytes;
        
        while(!exit){
            try{
                
                receivedBytes = input.read(data);
                
                if(receivedBytes > 0){
                    
                    size = (data[0] & 0xFF) << 8;
                    size = size | (data[1] & 0xFF);
                    
                    if(size > 0){
                        
                        for(int i = 0; i < size - 5; i++){
                            data2[i] = data[i+3];
                        }
                        
                        for(int i = size - 5; i < data2.length; i++){
                            data2[i] = '\0';
                        }
                        
                        crc = server.CRC16(data, size-2);
                        
                        crcValue = data[size - 2] & 0xFF;
                        crcValue = crcValue << 8;
                        crcValue = crcValue | (data[size - 1] & 0xFF);
                        
                        if(crcValue == crc){
                            manager.getCommand(data[2]).process(data2);
                        }else{
                            //TODO: criar else.
                        }
                    }else{
                        server.interruptServer();
                        exit = true;
                    }
                }else if (receivedBytes < 0 || !socket.isConnected()){
                    server.interruptServer();
                    exit = true;
                }
            Thread.sleep(50);
                
            }catch(InterruptedException | IOException e){
                exit = true;
                server.interruptServer();
            }
        }
    }
    
    public void stopThread(){
        if(input != null){
            try{
                input.close();
            } catch ( IOException e){
            }
        }
        input = null;
        socket = null;
        exit = true;
    }
    
}
