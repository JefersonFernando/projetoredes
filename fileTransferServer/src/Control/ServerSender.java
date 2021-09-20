/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.nio.ByteBuffer;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.io.*;

/**
 *
 * @author jefer
 */
public final class ServerSender implements Runnable{
    
    private static ServerSender instance = null;
    private volatile boolean exit = false;      /** Indica se a thread será encerrada. **/
    private Socket socket = null;        /** OutputStream para enviar dados. **/
    private OutputStream output;
    private  BlockingQueue<byte[]> queue;       /** Fila bloqueante para tratar o envio dos dados. **/
    private Server server = null;

    /**
     * @brief Construtor do Sender.
    */    
    private ServerSender(){
    }
    
    public void setServer(Server server){
        this.server = server;
    }
    
    public static ServerSender getInstance(){
        if(instance == null){
            instance = new ServerSender();
        }
        return instance;
    }
    
    public void setSocket(Socket socket){
        this.socket = socket;
        if(socket != null){
            try{
                output = socket.getOutputStream();
            } catch (IOException e){
                exit = true;
                server.interruptServer();
            }
        }
    }
    
    public void setQueue(BlockingQueue<byte[]> q){
        this.queue = q;
    }
    
    /**
     * @brief Método de execução na thread 
    */
    @Override
    public void run(){
        ByteBuffer buf = ByteBuffer.allocate(65536);
        exit = false;
        byte[] data;
        int err;
        while(!exit){
            try{
                if(queue != null && !queue.isEmpty()){
                    
                    data = this.queue.take();
                    output.write(data);
                }
                Thread.sleep(50);
            }catch(InterruptedException|IOException e){
                exit = true;
                server.interruptServer();
            }
        }
    } 
    
    /**
     * @brief Para a thread.
    */   
    public void stopThread(){
        this.exit = true;
        queue = null;
        socket = null;
    }
}
