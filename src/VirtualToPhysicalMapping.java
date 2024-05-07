//This class is used for virtual to physical mapping to disk mapping.
public class VirtualToPhysicalMapping {
    public int physicalPageNum;
    public int diskPageNum;

    public VirtualToPhysicalMapping(){
        physicalPageNum = -1;
        diskPageNum = -1;
    }
}
