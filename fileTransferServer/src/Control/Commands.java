/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Control;

import java.util.HashMap;

enum OptionCommands{
    START(1),
    SEND(2),
    RECEIVE(3),
    INVALID(0);
    
    private final int value;
    
    private static final HashMap<Integer, OptionCommands> BY_LABEL
                                = new HashMap<Integer, OptionCommands>();
    
    static{
        for(OptionCommands e : values()){
            BY_LABEL.put(e.value, e);
        }
    }
    
    OptionCommands(int value){
        this.value = value;
    }
    
    public int getValue(){
        return value;
    }
    
    public static OptionCommands valueOfLabel(int label) {
        return BY_LABEL.get(label);
    }
    
}

public class Commands{
    private final HashMap<OptionCommands, BasicCommand> commands;
    private static Commands instance = null;
    
    private Commands(){
        commands = new HashMap<>();
        commands.put(OptionCommands.START, StartCommand.getInstance());
    }
    
    public static Commands getInstance(){
        if(instance == null){
            instance = new Commands();
        }
        return instance;
    }
    
    public BasicCommand getCommand(int cmd){
        if (commands.containsKey(OptionCommands.valueOfLabel(cmd))){
            return commands.get(OptionCommands.valueOfLabel(cmd));
        }
        return commands.get(OptionCommands.INVALID);
    }
    
}
