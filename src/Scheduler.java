import java.util.*;

public class Scheduler{
    private LinkedList<PCB> listOfRealTime;
    private LinkedList<PCB> listOfInteractive;
    private LinkedList<PCB> listOfBackground;
    private LinkedList<PCB> listOfSleepingProcesses;
    private HashMap<Integer, PCB> listOfProcesses;
    private HashMap<Integer, PCB> listOfWaiting;
    private Timer timer;
    public PCB currentProcess;
    private Kernel kernel;

    public Scheduler(Kernel k){
        //Initialzes all members
        kernel = k;
        listOfRealTime = new LinkedList<>();
        listOfInteractive = new LinkedList<>();
        listOfBackground = new LinkedList<>();
        listOfSleepingProcesses = new LinkedList<>();
        listOfProcesses = new HashMap<>();
        listOfWaiting = new HashMap<>();
        timer = new Timer();
        //Creates the schedule interrupt with a 250ms cycle.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(currentProcess != null) {
                    currentProcess.process.requestStop();
                    //when requestStop(), increment the amount and call demotion.
                    currentProcess.requestStopAmount++;
                    demotion();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 250);
    }

    public int CreateProcess(PCB up){
        //Adds the process to the list
        addToLists(up);
        //adds the process to the hashmap of processes.
        listOfProcesses.put(up.GetPid(), up);
        //If there is nothing running or the current process is done, call SwitchProcess
        if(currentProcess == null || currentProcess.isDone()){
            emptyTLB();
            SwitchProcess();
        }
        //Update the processID and return it
        return currentProcess.pid;
    }

    public void SwitchProcess(){
        //Check if the currentProcess is null or isDone before adding to the list.
        if(currentProcess != null){
            if(!currentProcess.isDone()){
                //No need to add null or a process that is done to the list.
                addToLists(currentProcess);
            }else{
                //remove the process once done from the hashmap of processes.
                listOfProcesses.remove(currentProcess);
                //if the currentProcess is done, close all devices related to currentProcess
                try{
                    kernel.freeAllDevices();
                }catch (Exception ignored){ }
            }
        }

        //Checks if the list is empty, this is mainly for startup
        if(!allListEmpty()){
            //In order to avoid a race condition, the thread sleeps.
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){ }
            //Then it grabs the first process in the list and execute it.
            emptyTLB();
            determineProcess();
        }
    }

    public void Sleep(int milliseconds){
        //check if the process is null or the process is finished.
        if(currentProcess != null){
            if(!currentProcess.isDone()){
                //save the sleep time in PCB.
                currentProcess.minimumTime = System.currentTimeMillis() + milliseconds;
                //add to the list of sleeping processes.
                listOfSleepingProcesses.add(currentProcess);
            }else{
                freeProcessMemory();
                //if currentProcess is done, close all devices related to currentProcess
                try{
                    kernel.freeAllDevices();
                }catch (Exception ignored){ }
            }
        }

        //check if all lists are empty.
        if(allListEmpty()){
            //If so, we need to wait for a process to enter the queue.
            try{
                //we wait for the milliseconds so it is "sleeping" for that duration.
                Thread.sleep(milliseconds);
            }catch (InterruptedException e){ }
            //then we can remove the sleeping process to wake it up.
            listOfSleepingProcesses.remove(currentProcess);
        }else{
            //If there are processes in the lists, we sleep and call determineProcess()
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
            emptyTLB();
            determineProcess();
        }

        //Now we want to see if any of the sleeping processes can wake up.
        //No need to check the last sleeping process since it was just put to sleep.
        for(int i = 0; i < listOfSleepingProcesses.size()-1;i++){
            //If they are allowed to wake up, remove from the sleeping list and add it to the correct list
            //based on priority.
            if(listOfSleepingProcesses.get(i).minimumTime < System.currentTimeMillis()){
                PCB awakePCB = listOfSleepingProcesses.remove(i);
                addToLists(awakePCB);
            }
        }
    }

    //return true if all the lists are empty.
    public boolean allListEmpty(){
        return listOfRealTime.isEmpty() && listOfInteractive.isEmpty() && listOfBackground.isEmpty();
    }

    //used to put the process into the correct list/queue.
    public void addToLists(PCB newProcess){
        switch(newProcess.priority){
            case RealTime:
                listOfRealTime.add(newProcess);
                break;
            case Interactive:
                listOfInteractive.add(newProcess);
                break;
            case Background:
                listOfBackground.add(newProcess);
                break;
        }
    }

    //This method is used to determine what process get to run this time.
    public void determineProcess(){
        //we get a random number between 0 - 9
        Random rand = new Random();
        int randNum = rand.nextInt(10);
        //if the number is 4, 5, 6, 7, 8, 9 (6/10), we choose a RealTime process.
        if(randNum > 3){//RealTime
            //If the listOfRealTime is empty, we choose a different process
            if(listOfRealTime.isEmpty()){
                //Get a number between 0 - 3
                randNum = rand.nextInt(4);
                //If the number is 1, 2, 3 (3/4), we choose a Interactive Process.
                if(randNum > 0){//Interactive
                    //If the Interactive list is empty, we just choose a background process.
                    if(listOfInteractive.isEmpty()){
                        currentProcess = listOfBackground.pollFirst();
                    }else{
                        currentProcess = listOfInteractive.pollFirst();
                    }
                }else{//Background
                    //if the number is 0, we choose a Background Process.
                    //If the list of background is empty, we just choose a interactive process.
                    if(listOfBackground.isEmpty()){
                        currentProcess = listOfInteractive.pollFirst();
                    }else{
                        currentProcess = listOfBackground.pollFirst();
                    }
                }
            }else{
                //If there are processes in realTime, we just take the process at the head.
                currentProcess = listOfRealTime.pollFirst();
            }
        }else if(randNum > 0){//Interactive, if the number is 1, 2, 3, (3/10), we choose a Interactive Process.
            //If the list of interactives is empty, we need to choose a new process.
            if(listOfInteractive.isEmpty()){
                //Get a number between 0 - 3
                randNum = rand.nextInt(4);
                //If the number is 1, 2, 3 (3/4), then we choose a RealTime process
                if(randNum > 0){//RealTime
                    //If the RealTime list is empty, we just choose a background process
                    if(listOfRealTime.isEmpty()){
                        currentProcess = listOfBackground.pollFirst();
                    }else{
                        //If the real list is not empty, we can poll from the list.
                        currentProcess = listOfRealTime.pollFirst();
                    }
                }else{//Background, if the number is 0 (1/4), then we choose a Background process
                    //if the background list is empty, we just choose from realtime
                    if(listOfBackground.isEmpty()){
                        currentProcess = listOfRealTime.pollFirst();
                    }else{
                        //if the background list is not empty, we just poll from that list.
                        currentProcess = listOfBackground.pollFirst();
                    }
                }
            }else{
                //if there are Interactive processes, we can poll from that list.
                currentProcess = listOfInteractive.pollFirst();
            }
        }else{//Background, if the number is 0, we choose a background process.
            //if the list of background process is empty, we need to choose a new process.
            if(listOfBackground.isEmpty()){
                //get a number between 0 - 3
                randNum = rand.nextInt(4);
                if(randNum > 0){//RealTime, if the number is 1, 2, 3 (3/4), we choose a realtime process
                    //If the list of realtime is empty, we choose an interactive process
                    if(listOfRealTime.isEmpty()){
                        currentProcess = listOfInteractive.pollFirst();
                    }else{
                        //if the list of realtime is not empty, we can just poll from the list.
                        currentProcess = listOfRealTime.pollFirst();
                    }
                }else{//Interactive
                    //if the number is 0, we choose a interactive process
                    //if the list of interactive is empty, we choose a realtime process
                    if(listOfInteractive.isEmpty()){
                        currentProcess = listOfRealTime.pollFirst();
                    }else{
                        //if the list of interactive is not empty, we just poll from the list.
                        currentProcess = listOfInteractive.pollFirst();
                    }
                }
            }else{
                //if the list of background is not empty, we can just poll from the list.
                currentProcess = listOfBackground.pollFirst();
            }
        }
    }
    public void demotion(){
        //if the amount of requestStop is greater than 5, we need to demote.
        if(currentProcess.requestStopAmount > 4){
            //determine the current priority.
            switch(currentProcess.priority){
                //RealTime demotes to Interactive.
                case RealTime:
                    currentProcess.priority = Priority.Interactive;
                    break;
                //Interactive demotes to Background.
                case Interactive:
                    currentProcess.priority = Priority.Background;
                    break;
                //No need to demote Background since it is the lowest level.
            }

            //These System statements are used for debugging.
            //System.out.println("Process: " + currentProcess.toString() + " demoted.");
            //System.out.println("Now Priority: " + currentProcess.priority);

            //Reset the amount of requestStop.
            currentProcess.requestStopAmount = 0;
        }
    }
    public PCB getCurrentlyRunning(){
        return currentProcess;
    }

    public int GetPid(){
        return currentProcess.GetPid();
    }

    public int GetPidByName(String name){
        //Checks the list of processes and returns the corresponding PID by name.
        Set<Integer> keys = listOfProcesses.keySet();
        for(Integer k : keys){
            if(listOfProcesses.get(k).GetProcessName().equalsIgnoreCase(name)){
                return listOfProcesses.get(k).GetPid();
            }
        }
        //if it doesn't find the process in list, it means it doesn't exist, return -1.
        return -1;
    }
    public HashMap<Integer, PCB> getListOfProcesses(){
        return listOfProcesses;
    }
    public HashMap<Integer, PCB> getListOfWaiting(){
        return listOfWaiting;
    }
    public void freeProcessMemory(){
        //when a process is completed, you need to free both its PCB's array and its space in
        //the kernel's memory map so new processes can use this space.
        VirtualToPhysicalMapping[] processMemory = currentProcess.getMemoryPage();
        boolean[] memoryMap = kernel.getFreeSpace();
        for(int i = 0; i < processMemory.length;i++){
            int index = processMemory[i].physicalPageNum;
            memoryMap[index] = false;//false -> free space.
            processMemory[i].physicalPageNum = -1;//-1 -> free space.
            processMemory[i] = null;
        }
    }
    public void emptyTLB(){
        //when a process is switched, you need to empty out the TLB before switching to the new process.
        if(currentProcess != null){
            currentProcess.updateTLB(0, -1, -1);
            currentProcess.updateTLB(1, -1, -1);
        }
    }
    public PCB getRandomProcess(){
        //grabs all possible keys from the list of processes
        Set<Integer> keys = listOfProcesses.keySet();
        Random rand = new Random();
        while(true){
            //get a random key from the list of keys.
            int index = rand.nextInt(keys.size());
            //get the PCB assiocated to that key.
            PCB randomProcess = listOfProcesses.get(index);
            VirtualToPhysicalMapping[] randomProcessMap = randomProcess.getMemoryPage();
            //check through that process's map.
            for(VirtualToPhysicalMapping map : randomProcessMap){
                if(map != null){
                    //if the current map physical page number is not -1, then we can steal from this one.
                    if(map.physicalPageNum != 1){
                        return randomProcess;
                    }
                }
            }
        }
    }
}
