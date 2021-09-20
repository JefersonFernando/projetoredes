/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author jefer
 */
public final class Server implements Runnable{
    
    private static Server instance = null;
    private ServerSocket socket = null;
    private Socket client = null;
    private int port;
    private String ip;
    
    private final BlockingQueue<byte[]> queue;
    
    private ServerReceiver receiver = null;
    private ServerSender sender = null;
    
    private boolean exit = false;
    
    private GUIController interfaceController = null;
    
    private Server(){
        queue = new ArrayBlockingQueue<>(10);
        receiver = ServerReceiver.getInstance();
        sender = ServerSender.getInstance();
        receiver.setServer(this);
        sender.setServer(this);
    }
    
    public static Server getInstance(){
        if(instance == null){
            instance = new Server();
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
            return client.isConnected();
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
        
        if(port == -1){
            exit = true;
            interruptServer();
        }
        
        if(!exit){
            try{
                socket = new ServerSocket(port);
                socket.setSoTimeout(50);
                Thread.sleep(100);
                ip = InetAddress.getLocalHost().getHostAddress().trim();
                
                String systemIp = "-";
                
                try{
                    URL url = new URL("http://bot.whatismyipaddress.com");
                    
                    BufferedReader sc = new BufferedReader(new InputStreamReader(url.openStream()));
                    
                    systemIp = sc.readLine().trim();
                }catch (Exception e){
                    systemIp = "-";
                }
                
                interfaceController.ipField.clear();
                interfaceController.ipField.appendText(ip + "/" + systemIp);

            } catch(InterruptedException | IOException e){
                exit = true;
                interruptServer();
            }
        }
        
        while( (!exit) && client == null ){
            try{
                client = socket.accept();
                Thread.sleep(50);
            }catch(SocketTimeoutException e){
            }catch(InterruptedException | IOException e){
                exit = true;
                interruptServer();
            }
        }
        
        if(!exit && client.isConnected()){
            receiver.setSocket(client);
            sender.setSocket(client);
            sender.setQueue(queue);
            
            new Thread(receiver).start();
            new Thread(sender).start();
            
            interfaceController.connectedStatus();
        }else{
            interruptServer();
        }
        
        exit = true;
    }
    
    public void stopServer(){
        try{
            
            exit = true;
            
            if(receiver != null){
                receiver.stopThread();
            }
            
            if(sender != null){
                sender.stopThread();
            }
            
            Thread.sleep(100);
            
            if(client != null){
                client.close();
            }
            
            if(socket != null){
                socket.close();
            }
            
            socket = null;
            client = null;
            
            queue.clear();
            
        }catch(IOException | InterruptedException e){
            
        }
    }
    
    public void send(byte[] msg){
        try{
            int crc;
            int size = 0;
            
            size = (msg[0] & 0xFF) << 8;
            size = size | (msg[1] & 0xFF);
            size += 2;
            msg[0] = (byte)((size >> 8) & 0xFF);
            msg[1] = (byte)(size & 0xFF);
            
            crc = CRC16(msg, size - 2);
            msg = Arrays.copyOf(msg, size);
            
            msg[size - 2] = (byte)((crc >> 8) & 0xFF);
            msg[size - 1] = (byte)(crc & 0xFF);
            
            this.queue.put(msg);
            
        }catch(InterruptedException e){
            
        }
    }
    
    public void interruptServer(){
        interfaceController.connectionOff();
        exit = true;
    }
}