/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.nio.channels.*;

/**
 *
 * @author jefer
 */
public final class Client implements Runnable{
    
    private static Client instance = null;
    private SocketChannel socket = null;
    private int port;
    private String ip;
    
    private final BlockingQueue<byte[]> queue;
    
    private final ClientReceiver receiver = null;
    private final ClientSender sender = null;
    
    private boolean exit = false;
    
    private GUIController interfaceController = null;
    
    private Client(){
        queue = new ArrayBlockingQueue<>(10);
    }
    
    public static Client getInstance(){
        if(instance == null){
            instance = new Client();
        }
        return instance;
    }
    
    public void setInterfaceController(GUIController controller){
        this.interfaceController = controller;
    }
    
    public int CRC16(byte[] buffer, int length) {
        int crc = 0xffff;
        int polynomial = 0x1021;
        int divide, value;
        
        for(int i = 0; i < length ; i++){
            for(int b = 0; b < 8; b++){
                divide = crc & 0x8000;
                crc = crc << 1;
                value = buffer[i] & 0xFF;
                if( ( value & (0x80 >> b)) != 0 ){
                    crc |= 0x1; 
                }
                if ( divide != 0 ){
                    crc ^= polynomial;
                }
            }
        }
        
        return crc & 0xffff;
    }
    
    public boolean isConnected(){
        if(socket != null){
            return socket.isConnected();
        }
        return false;
    }
    
    public boolean isOn(){
        return (socket != null);
    }
    
    @Override
    public void run(){
        
        exit = false;
        
        ip = interfaceController.getIp();
        
        port = interfaceController.getPort();
        
        if("-".equals(ip) || port == -1){
            exit = true;
        }
        
        interfaceController.ipField.setEditable(false);
        interfaceController.portField.setEditable(false);
        
        if(!exit){
            try{
                socket = SocketChannel.open();
                socket.configureBlocking(false);

            } catch(IOException e){
                exit = true;
            }
        }
        
        if(!exit && !socket.isConnectionPending()){
            try{
                socket.connect(new InetSocketAddress(ip, port));
            }catch(IOException | IllegalArgumentException e){
                exit = true;
            }
        }else{
            exit = true;
        }
        
        while( (!exit) && (!socket.isConnected())){
            try{
                socket.finishConnect();
                Thread.sleep(50);
            }catch(InterruptedException | IOException e){
                exit = true;
            }
        }
        
        if(!exit && socket.isConnected()){
            
        }
        
        exit = true;
    }
    
    public void interruptClient(){
        exit = true;
    }
    
}
