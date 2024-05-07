import java.io.RandomAccessFile;
public class FakeFileSystem implements Device{
    private RandomAccessFile[] files = new RandomAccessFile[10];
    @Override
    public int open(String s) throws Exception {
        //if open is sent no name or empty name, it should throw an exception.
        if(s == null || s.isEmpty()){
            throw new Exception("Empty filename!");
        }
        //if it is a valid filename, then check if there is space in the array.
        for(int i = 0; i < files.length;i++){
            //if there is a index with null, that is a empty space.
            if(files[i] == null){
                //populate that index with a new file with "rw" mode enabled.
                files[i] = new RandomAccessFile(s, "rw");
                return i;//return the index, implies the file creation/open was successfully.
            }
        }
        return -1;//return -1 if there is no space for the file (failed)
    }

    @Override
    public void close(int id) throws Exception {
        //if the id is over the size of 9 or files[id] is not initialized, throw an exception
        if(id > 9 || files[id] == null){
            throw new Exception("Empty/Invalid File");
        }
        //call .close() on the randomAccessFile
        files[id].close();
        //null the value at that index.
        files[id] = null;
    }

    @Override
    public byte[] read(int id, int size) throws Exception {
        //if the id is over the size of 9, size is not a positive number, or files[id] is null, throw an exception.
        if(id > 9 || size < 0 || files[id] == null){
            throw new Exception("Empty/Invalid File!");
        }
        //create a new byte array
        byte[] retVal = new byte[size];
        files[id].read(retVal, 0, size);//using .read(), read from the randomAccessFile and store it in retVal.
        return retVal;
    }

    @Override
    public void seek(int id, int to) throws Exception {
        //if the id is over 9 or files[id] is null, throw an exception.
        if(id > 9 || files[id] == null){
            throw new Exception("Empty/Invalid File!");
        }
        //just call seek with to on the id in files.
        files[id].seek(to);
    }

    @Override
    public int write(int id, byte[] data) throws Exception {
        //if the id is over 9 and the files[id] is null, throw an exception.
        if(id > 9 || files[id] == null){
            throw new Exception("Invalid Position in FFS");
        }
        //just call write on data on the randomAccessFile.
        files[id].write(data);
        return 0;//return 0 as success.
    }
}
