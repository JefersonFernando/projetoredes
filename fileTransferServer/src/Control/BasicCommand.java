
package Control;


public abstract class BasicCommand {
    protected Client client;
    protected Server server;
    protected GUIController interfaceController = null;
    
    abstract void setInterfaceController(GUIController controller);
    
    abstract void process(byte[] bytes);
}
