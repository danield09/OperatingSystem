import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device{
    private Scheduler scheduler;
    private Thread thread;
    private Semaphore semaphore;
    private VFS vfs;
    private boolean[] freeSpace;//used to keep track on what pages are in used.

    public Kernel(){
        //Initializes all members.
        scheduler = new Scheduler(this);
        thread = new Thread(this);
        semaphore = new Semaphore(0);
        vfs = new VFS();
        freeSpace = new boolean[1000];
        thread.start();
    }
    public void start(){
        semaphore.release();
    }

    public void run(){
        while(true){
            try{
                //to see if thread should be running.
                semaphore.acquire();
            }catch (InterruptedException e){ break; }
            switch(OS.getCurrentCall()){
                case CreateProcess:
                    //call CreateProcess in Scheduler
                    OS.returnValue = scheduler.CreateProcess((PCB)OS.getParameters().get(0));
                    break;
                case SwitchProcess:
                    //call SwitchProcess in Scheduler
                    scheduler.SwitchProcess();
                    break;
                case Sleep:
                    //call Sleep in Scheduler
                    scheduler.Sleep((int)OS.getParameters().get(0));
                    break;
                case SendMessage:
                    //call it's SendMessage.
                    SendMessage((KernelMessage)OS.getParameters().get(0));
                case FetchMessage:
                    //calls it's WaitForMessage().
                    WaitForMessage();
                    break;
            }
            //Once scheduler is done, call start on the new currentProcess
            scheduler.currentProcess.run();
        }
    }

    public Scheduler getScheduler(){
        return scheduler;
    }

    public void freeAllDevices() throws Exception {
        //just the vfs ids from the current process (which is done)
        int[] vfsID = getScheduler().getCurrentlyRunning().ids;
        //loops through the array, closing each device and setting their value to -1 (for re-populating)
        for(int i = 0; i < 10;i++){
            vfs.close(vfsID[i]);
            getScheduler().getCurrentlyRunning().ids[i] = -1;
        }

        //Used for testing/debugging
        //System.out.println(getScheduler().getCurrentlyRunning().toString() + " has all devices freed!");
    }

    @Override
    public int open(String s) throws Exception {
        //get the array from the current process
        int[] PCBArray = getScheduler().getCurrentlyRunning().ids;
        int currID = -1;//used as a status checker.
        for(int i = 0; i < PCBArray.length;i++){
            //if there is a -1, there is space to create a new Device/Random
            if(PCBArray[i] == -1){
                currID = i;//set currID to that free index.
                break;
            }
        }
        //if there is no space, return -1;
        if(currID == -1)
            return -1;
        //call open on vfs
        int vfsRetID = vfs.open(s);
        if(vfsRetID == -1)//if vfsRetID returns -1, then there is no space in vfs.
            return -1;
        //if not, then set the value at currID in PCBArray to the free vfsRetID.
        PCBArray[currID] = vfsRetID;
        return currID;//return currID to say it was a success.
    }

    @Override
    public void close(int id) throws Exception {
        //calls to get vfsID that stored in "id" in the array in currentProcess
        int vfsID = getScheduler().getCurrentlyRunning().ids[id];
        vfs.close(vfsID);//calls close on that vfs
        getScheduler().getCurrentlyRunning().ids[id] = -1;//set to -1 for re-populating
    }

    @Override
    public byte[] read(int id, int size) throws Exception {
        //calls to get vfsID that stored in "id" in currentProcess
        int vfsID = getScheduler().getCurrentlyRunning().ids[id];
        return vfs.read(vfsID, size);//calls read with that vfsID and size
    }

    @Override
    public void seek(int id, int to) throws Exception {
        //calls to get vfsID that stored in "id" in currentProcess
        int vfsID = getScheduler().getCurrentlyRunning().ids[id];
        vfs.seek(vfsID, to);//calls seek with that vfsID and seek.
    }

    @Override
    public int write(int id, byte[] data) throws Exception {
        //calls to get vfsID that stored in "id" in currentProcess
        int vfsID = getScheduler().getCurrentlyRunning().ids[id];
        return vfs.write(vfsID, data);//calls write with that vfsID and data.
    }

    public int GetPid(){
        return scheduler.GetPid();
    }

    public int GetPidByName(String name){
        return scheduler.GetPidByName(name);
    }

    public void SendMessage(KernelMessage km){
        //create a copyMessage to avoid referencing the same object.
        KernelMessage copyMessage = new KernelMessage(km);

        //set the sender_pid in the copy message.
        copyMessage.setSender(OS.GetPid());
        //grabs the two lists from scheduler.
        HashMap<Integer, PCB> listOfProcesses = scheduler.getListOfProcesses();
        HashMap<Integer, PCB> listOfWaiting = scheduler.getListOfWaiting();


        //check if the process is a valid process in the OS system.
        //if so, get the corresponding PCB and add the message to its queue.
        if(listOfProcesses.containsKey(copyMessage.getTarget())){
            listOfProcesses.get(copyMessage.getTarget()).addMessage(copyMessage);
        }

        //check if the process is waiting for a message
        //if so, remove from the waiting list,
        //and add it back to the list of queues in scheduler.
        if(listOfWaiting.containsKey(copyMessage.getTarget())){
            PCB process = listOfWaiting.remove(copyMessage.getTarget());
            scheduler.addToLists(process);
        }
    }

    public KernelMessage WaitForMessage() {
        //grabs the list of waiting processes from scheduler
        HashMap<Integer, PCB> listOfWaiting = scheduler.getListOfWaiting();
        //see if there is a message from the currentProcess
        KernelMessage km = scheduler.currentProcess.getMessage();
        if(km != null) {
            //if there is a valid message, set it to the returnValue and return.
            OS.returnValue = km;
            return km;
        } else {
            //if not, then de-schedule the currentProcess and put it in the wait list.
            listOfWaiting.put(scheduler.currentProcess.GetPid(), scheduler.currentProcess);
            //save the currProcess before switching to a new process.
            PCB currProcess = scheduler.currentProcess;
            scheduler.determineProcess();

            //when there is a valid message, return that new message.
            km = currProcess.getMessage();
            OS.returnValue = km;
            return km;
        }
    }

    //return the memoryMap.
    public boolean[] getFreeSpace(){
        return freeSpace;
    }
}