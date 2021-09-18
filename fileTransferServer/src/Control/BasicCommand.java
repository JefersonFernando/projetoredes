/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

/**
 *
 * @author jefer
 */
public abstract class BasicCommand {
    private Client client;
    
    abstract void process(byte[] bytes);
    
    abstract void feed(String text);
}
