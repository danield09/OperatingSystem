public class KernelMessage {
    private int sender_pid;
    private int target_pid;
    private int type;
    private byte[] data;
    public KernelMessage(KernelMessage km){
        //copy all the info from km to this new object.
        this.sender_pid = km.sender_pid;
        this.target_pid = km.target_pid;
        this.type = km.type;
        this.data = km.data;
    }
    //this constructor is used for the processes to initialize a message.
    public KernelMessage(int sender, int target, int t, byte[] d){
        sender_pid = sender;
        target_pid = target;
        type = t;
        data = d;
    }
    //toString prints out all member's information.
    public String toString(){
        return "Sender: " + sender_pid + "\nTarget: " + target_pid + "\nType: " + type + "\nMessage: " + data.toString();
    }

    //Used in Kernel's SendMessage to confirm the sender.
    public void setSender(int sender){
        sender_pid = sender;
    }

    //Used in Kernel to determine if the target_pid is a valid process.
    public int getTarget(){
        return target_pid;
    }
}
