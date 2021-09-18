/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.io.*;

/**
 *
 * @author jefer
 */
public final class ClientSender implements Runnable{
    
    private static ClientSender instance = null;
    private volatile boolean exit = false;      /** Indica se a thread será encerrada. **/
    private SocketChannel socket = null;        /** OutputStream para enviar dados. **/
    private  BlockingQueue<byte[]> queue;       /** Fila bloqueante para tratar o envio dos dados. **/

    /**
     * @brief Construtor do Sender.
    */    
    private ClientSender(){
    }
    
    public static ClientSender getInstance(){
        if(instance == null){
            instance = new ClientSender();
        }
        return instance;
    }
    
    public void setSocket(SocketChannel socket){
        this.socket = socket;
    }
    
    public void setQueue(BlockingQueue<byte[]> q){
        this.queue = q;
    }
    
    /**
     * @brief Método de execução na thread 
    */
    @Override
    public void run(){
        ByteBuffer buf = ByteBuffer.allocate(256);
        exit = false;
        Client client = Client.getInstance();
        byte[] data;
        while(!exit){
            try{
                if(!queue.isEmpty()){
                    data = this.queue.take();
                    buf.clear();
                    buf.put(data);
                    buf.flip();
                    while(buf.hasRemaining()){
                        socket.write(buf);
                    }
                    buf.clear();
                }
            }catch(InterruptedException|IOException e){
                client.interruptClient();
                exit = true;
            }   
        }
    }
    
    /**
     * @brief Para a thread.
    */   
    public void stopThread(){
        this.exit = true;
    }
}
