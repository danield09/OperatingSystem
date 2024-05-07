public class VFS implements Device{
    private Device[] devices = new Device[10];
    private int[] devicesID = new int[10];
    private int[] vfsID = new int[10];
    @Override
    public int open(String s) throws Exception {
        //split is used to know what the code needs to create.
        String[] input = s.split(" ", 2);
        if(input[0].equals("random")){//if the first string is random, we need to create a random object
            //loops through the array, looking for space.
            for(int i = 0; i < vfsID.length;i++){
                //if the device is null and the devicesID is 0, there is space for the new random object.
                if(devices[i] == null && devicesID[i] == 0){
                    //create a randomDevice.
                    RandomDevice newDevice = new RandomDevice();
                    int deviceID = newDevice.open(input[1]);//calls open on new object with the rest of input.
                    vfsID[i] = i;//claims the space in vfsID
                    devices[i] = newDevice;//sets its device in devices
                    devicesID[i] = deviceID;//sets its deviceID in devicesID
                    return i;//returns i in order to say it was successful.
                }
            }
        }else if(input[0].equals("file")){//if the first string is file, we create a new file.
            //loops through the array.
            for(int i = 0; i < vfsID.length;i++){
                //if the device is null, and the devices is 0, there is space for a new file.
                if(devices[i] == null && devicesID[i] == 0){
                    //create the new file.
                    FakeFileSystem newFile = new FakeFileSystem();
                    int fileID = newFile.open(input[1]);//calls open on new file with the rest of input.
                    vfsID[i] = i;//claims the space in vfsID
                    devices[i] = newFile;//sets its file object in devices
                    devicesID[i] = fileID;//sets its fileID in devicesID
                    return i;//returns i in order to say it was successful.
                }
            }
        }
        return -1;//returns -1 if there was no space for the new device (failed)
    }

    @Override
    public void close(int id) throws Exception {
        //if the id is over 9, throw an exception
        if(id > 9){
            throw new Exception("Invalid ID");
        }
        devices[id] = null;//nulls the device at id
        devicesID[id] = 0;//sets the deviceID to 0
        vfsID[id] = 0;//sets the vfsID to 0.
    }

    @Override
    public byte[] read(int id, int size) throws Exception {
        //if the id is over 9, size is not a positive number, or devices[id] is not a valid device, throw an exception
        if(id > 9 || size < 0 || devices[id] == null){
            throw new Exception("Invalid parameters!");
        }
        //calls read on that device and returns the result.
        return devices[id].read(devicesID[id], size);
    }

    @Override
    public void seek(int id, int to) throws Exception {
        //if the id is over 9, or devices[id] is not a valid device, throw an exception.
        if(id > 9 || devices[id] == null){
            throw new Exception("Invalid parameters");
        }
        //calls seek on that device
        devices[id].seek(devicesID[id], to);
    }

    @Override
    public int write(int id, byte[] data) throws Exception {
        //if the id is over 9, or devices[id] is not a valid device, throw an exception.
        if(id > 9 || devices[id] == null){
            throw new Exception("Invalid parameters");
        }
        //calls write on that device and returns that result.
        return devices[id].write(devicesID[id], data);
    }
}
