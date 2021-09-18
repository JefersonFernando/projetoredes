/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.io.*;

/**
 *
 * @author jefer
 */
public final class ClientReceiver implements Runnable{
    
    private static ClientReceiver instance = null;
    private volatile boolean exit = false;
    private SocketChannel socket = null;
    
    private Map<OptionCommands, Commands> commands;
    
    private ClientReceiver(){
    }
    
    public static ClientReceiver getInstance(){
        if(instance == null){
            instance = new ClientReceiver();
        }
        return instance;
    }
    
    public void setSocket(SocketChannel socket){
        this.socket = socket;
    }
    
    public void run(){
        exit = false;
        Client client = Client.getInstance();
        ByteBuffer buff = ByteBuffer.allocate(256);
        byte[] data2 = new byte[256];
        byte[] data;
        int size, crc;
        
        Commands manager = Commands.getInstance();
        long crcValue;
        int receivedBytes;
        
        while(!exit){
            try{
                receivedBytes = socket.read(buff);
                
                if(receivedBytes > 0){
                    data = buff.array();
                    buff.clear();
                    
                    size = data[0] & 0xFF;
                    
                    if(size > 0){
                        
                        for(int i = 0; i < size - 4; i++){
                            data2[i] = data[i+2];
                        }
                        
                        for(int i = size - 4; i < data2.length; i++){
                            data2[i] = '\0';
                        }
                        
                        crc = client.CRC16(data, size-2);
                        
                        crcValue = data[size - 2] & 0xFF;
                        crcValue = crcValue << 8;
                        crcValue = crcValue | (data[size - 1] & 0xFF);
                        
                        if(crcValue == crc){
                            manager.getCommand(data[1]).process(data2);
                        }else{
                            //TODO: criar else.
                        }
                    }else{
                        client.interruptClient();
                        exit = true;
                    }
                }else if (receivedBytes < 0 || !socket.isConnected()){
                    client.interruptClient();
                    exit = true;
                }
            }catch(IOException e){
                client.interruptClient();
                exit = true;
            }
        }
    }
    
    public void stopThread(){
        exit = true;
    }
    
}
