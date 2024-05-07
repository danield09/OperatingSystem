import java.util.ArrayList;
import java.util.HashMap;

enum CallType{
    CreateProcess, SwitchProcess, Sleep, FetchMessage, SendMessage
}
public class OS{
    private static Kernel kernel;
    private static CallType currentCall;
    private static ArrayList<Object> parameters = new ArrayList<>();
    public static Object returnValue;
    private static int swapFileID;
    private static int swapPageTracker;

    public static int CreateProcess(UserlandProcess up){
        parameters.clear();//Reset the parameters
        PCB processPCB = new PCB(up);
        parameters.add(processPCB);//Add the new parameters to the parameter list.
        currentCall = CallType.CreateProcess;//Set the currentCall
        kernel.start();//Switch to the Assignment2.Kernel.

        //If there is a process currently running, stop it.
        if(kernel.getScheduler().currentProcess != null){
            kernel.getScheduler().currentProcess.stop();
        }
        //This is for startup where there is no currentProcess
        //Loop with sleep(10) until there is a currentProcess available.
        while(kernel.getScheduler().currentProcess == null){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
        }
        return (int)(returnValue);//Cast and return the return value.
    }

    public static int CreateProcess(UserlandProcess up, Priority pr){
        parameters.clear();
        PCB processPCB = new PCB(up, pr);
        parameters.add(processPCB);//Add the new parameters to the parameter list.
        currentCall = CallType.CreateProcess;//Set the currentCall
        kernel.start();//Switch to the Assignment2.Kernel.

        //If there is a process currently running, stop it.
        if(kernel.getScheduler().currentProcess != null){
            kernel.getScheduler().currentProcess.stop();
        }
        //This is for startup where there is no currentProcess
        //Loop with sleep(10) until there is a currentProcess available.
        while(kernel.getScheduler().currentProcess == null){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
        }
        return (int)(returnValue);//Cast and return the return value.
    }



    public static void Startup(UserlandProcess init) throws Exception {
        kernel = new Kernel();//Creates the Assignment2.Kernel
        CreateProcess(init);//Calls CreateProcess on init
        CreateProcess(new IdleProcess());//Calls CreateProcess on a new Assignment2.IdleProcess
        swapFileID = open("file swapFile");
        swapPageTracker = -1;
    }

    public static void SwitchProcess(){
        parameters.clear();//Clear the parameters.
        parameters.add(kernel.getScheduler().currentProcess);//Add new parameters to the list
        currentCall = CallType.SwitchProcess;//Set the currentCall
        PCB currProcess = kernel.getScheduler().currentProcess;
        kernel.start();//Switch to Assignment2.Kernel
        //If there is a process running, stop it.
        if(currProcess != null){
            if(!currProcess.isDone()){
                currProcess.stop();
            }
        }
    }

    public static void Sleep(int milliseconds){
        parameters.clear();//Reset the parameters
        parameters.add(milliseconds);//Add the new parameters to the parameter list.
        currentCall = CallType.Sleep;//Set the currentCall
        kernel.start();//Switch to the Assignment2.Kernel.

        if(kernel.getScheduler().currentProcess != null){
            kernel.getScheduler().currentProcess.stop();
        }

        while(kernel.getScheduler().currentProcess == null){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
        }
    }

    public static CallType getCurrentCall(){
        return currentCall;
    }
    public static ArrayList<Object> getParameters(){
        return parameters;
    }

    public static int open(String s) throws Exception {
        return kernel.open(s);//calls kernel's open
    }

    public static void close(int id) throws Exception {
        kernel.close(id);//calls kernel's close
    }

    public static byte[] read(int id, int size) throws Exception {
        return kernel.read(id, size);//calls kernel's read
    }

    public static void seek(int id, int to) throws Exception {
        kernel.seek(id, to);//calls kernel's seek
    }

    public static int write(int id, byte[] data) throws Exception {
        return kernel.write(id, data);//calls kernel write
    }

    public static int GetPid(){
        return kernel.GetPid();
    }

    public static int GetPidByName(String name){
        return kernel.GetPidByName(name);
    }

    public static void SendMessage(KernelMessage km){
        //clears parameters, add the message to parameters
        //sets the callType and start the kernel.
        parameters.clear();
        parameters.add(km);
        currentCall = CallType.SendMessage;
        kernel.start();

        //If there is a process currently running, stop it.
        if(kernel.getScheduler().currentProcess != null){
            kernel.getScheduler().currentProcess.stop();
        }

        //This is for startup where there is no currentProcess
        //Loop with sleep(10) until there is a currentProcess available.
        while(kernel.getScheduler().currentProcess == null){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
        }
    }

    public static KernelMessage WaitForMessage(){
        parameters.clear();//Reset the parameters
        currentCall = CallType.FetchMessage;//Set the currentCall
        kernel.start();//starts up kernel.

        //If there is a process currently running, stop it.
        if(kernel.getScheduler().currentProcess != null){
            kernel.getScheduler().currentProcess.stop();
        }

        //This is for startup where there is no currentProcess
        //Loop with sleep(10) until there is a currentProcess available.
        while(kernel.getScheduler().currentProcess == null){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
        }
        //returns the message set back Kernel's WaitForMessage.
        return (KernelMessage) OS.returnValue;
    }

    public static void GetMapping(int virtualPageNumber) throws Exception {
        //grabs the certain map we need.
        VirtualToPhysicalMapping[] processMemory = kernel.getScheduler().currentProcess.getMemoryPage();
        VirtualToPhysicalMapping memLocation = processMemory[virtualPageNumber];
        int newPhysicalPage = -1;
        //if the certain map is -1, then we first need to check for any space in the boolean map.
        if(memLocation.physicalPageNum == -1){
            boolean[] memoryMap = kernel.getFreeSpace();
            for(int i = 0; i < memoryMap.length;i++){
                //if there is space...
                if(!memoryMap[i]){
                    //that index is the physical page
                    newPhysicalPage = i;
                    memoryMap[i] = true;//set the spot to true, meaning it is in used.
                    break;//break out of the loop since we are done.
                }
            }

            //if the physical page is still -1, then we need to steal from another process.
            if(newPhysicalPage == -1){
                //call getRandomProcess with an available spot to steal from.
                PCB victimProcess = kernel.getScheduler().getRandomProcess();
                //grabs its memory map.
                VirtualToPhysicalMapping[] randomProcessMap = victimProcess.getMemoryPage();
                for(int i = 0; i < randomProcessMap.length;i++){
                    //check if this currentMap is the one with available spot.
                    VirtualToPhysicalMapping currentMap = randomProcessMap[i];
                    if(currentMap != null && currentMap.physicalPageNum != -1){
                        //save the victim's physical page.
                        int victimPhysicalPage = currentMap.physicalPageNum;
                        //get the data from MainMemory, and write into the swapfile.
                        byte[] memoryData = new byte[]{victimProcess.process.Read(i)};
                        write(swapFileID, memoryData);
                        //set the physical page to -1 on the victim side.
                        currentMap.physicalPageNum = -1;
                        //set the disk page to the page tracker on the victim side.
                        currentMap.diskPageNum = swapPageTracker;
                        swapPageTracker++;//increment the file tracker.
                        //set the physical page of the current process to the victim's physical page.
                        memLocation.physicalPageNum = victimPhysicalPage;
                        //set the newPhysicalPage in order to update the TLB properly.
                        newPhysicalPage = victimPhysicalPage;
                    }
                }
            }
        }else{
            //if the newPhysicalPage is not -1, then there is a spot already, use that index.
            newPhysicalPage = memLocation.physicalPageNum;
        }

        //This is to randomly selected one of the two entries TLB to update.
        int randNum = (int)(Math.random());
        int index;
        if(randNum % 2 == 0){
            index = 0;
        }else{
            index = 1;
        }
        //update the TLB.
        kernel.getScheduler().currentProcess.updateTLB(index, virtualPageNumber, newPhysicalPage);
    }
    public static int AllocateMemory(int size){
        //if the size is not a multiple of 1024, fail.
        if(size % 1024 != 0){
            return -1;
        }

        int amountOfPage = size / 1024;
        int check = 0;
        int startingAddress = -1;
        //get the memory page from the current process.
        VirtualToPhysicalMapping[] memoryPage = kernel.getScheduler().currentProcess.getMemoryPage();
        //check if there is enough space in the PCB's memory page that is NOT promised already (null).
        for(int i = 0; i < memoryPage.length;i++){
            for(int j = i; j < i + amountOfPage;j++){
                if(j < 100 && memoryPage[j] == null){
                    check++;
                }
            }
            //if there is a continuous path of null, there is enough space for this allocation.
            if(check == amountOfPage){
                //initialize the path in the PCB's map.
                for(int k = i; k < i + amountOfPage;k++){
                    kernel.getScheduler().currentProcess.getMemoryPage()[k] = new VirtualToPhysicalMapping();
                }
                //save the starting address.
                startingAddress = i;
                break;
            }else{
                check = 0;
            }
        }
        //return the starting address.
        return startingAddress;
    }
    public static boolean FreeMemory(int pointer, int size){
        //if either pointer or size is not a multiple of 1024, return fail.
        if(pointer % 1024 != 0 || size % 1024 != 0){
            return false;
        }

        int point = pointer / 1024;
        int page = size / 1024;

        VirtualToPhysicalMapping[] processMap = kernel.getScheduler().currentProcess.getMemoryPage();
        int counter = 0;
        //check if there is a straight path from pointer to point+page where processMap.physicalPageNum is not -1,
        //meaning it is not promised and being in used.
        for(int i = point; i < point+page;i++){
            if(processMap[point].physicalPageNum != -1){
                counter++;
            }
        }

        //if counter is equal to page, then fail.
        if(counter != page){
            return false;
        }

        //if counter equals page, then we can confirm it is enough space to free and set null in the PCB's array.
        for(int i = point; i < point+page;i++){
            processMap[i] = null;
        }
        //return true as it is successful.
        return true;
    }
}
