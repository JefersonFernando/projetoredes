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
    
    @Override
    public void run(){
        exit = false;
        Client client = Client.getInstance();
        ByteBuffer buff = ByteBuffer.allocate(65536);
        byte[] data2 = new byte[65536];
        byte[] data;
        
        Commands manager = Commands.getInstance();
        
        int receivedBytes;
        
        while(!exit){
            try{
                receivedBytes = socket.read(buff);
                
                if(receivedBytes > 0){
                    data = buff.array();
                    buff.clear();
                        
                    for(int i = 0; i < receivedBytes - 1; i++){
                        data2[i] = data[i+1];
                    }

                    for(int i = receivedBytes - 1; i < data2.length; i++){
                        data2[i] = '\0';
                    }
                    
                    manager.getCommand(data[0]).process(data2);
                    
                }else if (receivedBytes < 0 || !socket.isConnected()){
                    client.interruptClient();
                    exit = true;
                }
                Thread.sleep(50);
            }catch( InterruptedException | IOException e){
                client.interruptClient();
                exit = true;
            }
        }
    }
    
    public void stopThread(){
        socket = null;
        exit = true;
    }
    
}
