import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

enum Priority {
    RealTime, Interactive, Background
}
public class PCB {
    private static int next_pid;
    protected int pid;
    protected Priority priority;
    protected UserlandProcess process;
    protected long minimumTime;
    protected int requestStopAmount;
    protected int[] ids;//used to store the vfsID for Devices.
    protected String processName;
    protected LinkedList<KernelMessage> messageQueue;
    private VirtualToPhysicalMapping[] memoryPage;//used to keep track what memory spaces this PCB has access to.
    public PCB(UserlandProcess up){
        //Initializes member, default priority to Interactive.
        process = up;
        priority = Priority.Interactive;
        pid = next_pid;
        ids = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};//all -1 to indicates there is space.
        next_pid++;
        requestStopAmount = 0;
        processName = up.getClass().getSimpleName();
        messageQueue = new LinkedList<>();
        memoryPage = new VirtualToPhysicalMapping[100];
    }
    public PCB(UserlandProcess up, Priority pr){
        //Initializes member, default priority to "pr".
        process = up;
        priority = pr;
        ids = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};//all -1 to indicates there is space.
        pid = next_pid;
        next_pid++;
        requestStopAmount = 0;
        processName = up.getClass().getSimpleName();
        messageQueue = new LinkedList<>();
        memoryPage = new VirtualToPhysicalMapping[100];
    }

    public void stop(){
        process.stop();
        while(!process.isStopped()){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
        }
    }

    public boolean isDone(){
        return process.isDone();
    }
    public void run(){
        process.start();
    }
    public int GetPid(){
        return pid;
    }
    public String GetProcessName(){
        return processName;
    }
    public void addMessage(KernelMessage km){
        //adds message to queue
        messageQueue.add(km);
    }
    public KernelMessage getMessage(){
        //takes the first message from the queue, return null if empty.
        return messageQueue.pollFirst();
    }

    public VirtualToPhysicalMapping[] getMemoryPage(){
        //return this PCB's memory page.
        return memoryPage;
    }

    public void updateTLB(int index, int vPage, int pPage){
        //this method is just used to update the TLB in UserlandProcess.
        process.updateTLB(index, vPage, pPage);
    }

    //This method is mainly used for testing/debugging.
    public String toString(){
        return process.toString();
    }
}
