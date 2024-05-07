import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable{
    private Thread thread;
    private Semaphore semaphore;
    private boolean quantumExpired;
    public static byte[] physicalMemory;
    private static int[][] TLB;

    public UserlandProcess(){
        //Initializes all member.
        thread = new Thread(this);
        semaphore = new Semaphore(0);
        quantumExpired = false;
        if(physicalMemory == null)
            physicalMemory = new byte[1048576];
        TLB = new int[2][2];
        TLB[0][0] = -1;
        TLB[1][0] = -1;
        thread.start();
    }
    public void requestStop(){
        quantumExpired = true;
    }
    public abstract void main() throws Exception;
    public boolean isStopped(){
        //When there is no permits, the thread stops.
        return semaphore.availablePermits() == 0;
    }
    public boolean isDone(){
        return !thread.isAlive();
    }
    public void start(){
        semaphore.release();

    }
    public void stop(){
        try{
            semaphore.acquire();
        }catch (InterruptedException e){ }
    }
    public void run(){
        try{
            semaphore.acquire();
            main();
        }catch (InterruptedException e){ } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void cooperate(){
        //When the quantum expires, we want to switch the processes.
        if(quantumExpired){
            quantumExpired = false;
            OS.SwitchProcess();
        }
    }

    public void Sleep(int milli){
        OS.Sleep(milli);
    }

    public byte Read(int address) throws Exception {
        return physicalMemory[determineAddress(address)];
    }

    public void Write(int address, byte value) throws Exception {
        physicalMemory[determineAddress(address)] = value;
    }

    public int determineAddress(int virtualAddress) throws Exception {
        //virtualPage = virtualAddress / 1024
        int virtualPage = virtualAddress / 1024;
        int physicalPage = -1;
        while(physicalPage == -1){
            //check the TLB if the virtualPage is there.
            if(TLB[0][0] == virtualPage){
                //if so, we have a physical page.
                physicalPage = TLB[0][1];
            }else if(TLB[1][0] == virtualPage){
                //if so, we have a physical page.
                physicalPage = TLB[1][1];
            }else{
                //if not, we need to call GetMapping and try again.
                OS.GetMapping(virtualPage);
            }
        }
        //then the formula is (physicalPage * 1024) + Page offset (virtualAddress % 1024)
        return (physicalPage * 1024) + (virtualAddress % 1024);
    }

    public void updateTLB(int index, int vPage, int pPage){
        //mainly used to update the TLB (either with a new virtualPage -> physicalPage or to empty it out.
        TLB[index][0] = vPage;
        TLB[index][1] = pPage;
    }
}