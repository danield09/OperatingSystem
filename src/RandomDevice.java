import java.util.Random;
public class RandomDevice implements Device{
    private Random[] items = new Random[10];
    @Override
    public int open(String s) {
        //loops through the array, checking for space.
        for(int i = 0; i < items.length;i++){
            //if there is a null value, there is space.
            if(items[i] == null){
                //if there is a valid seed (not empty or null) then we can use it as the random seed.
                if(s != null && !s.isEmpty()){
                    int seed = Integer.parseInt(s);//assume it is a valid integer.
                    items[i] = new Random(seed);//creates the random object.
                    return i;//returns the index to say it was successful in opening/creating.
                }else{
                    //if there is no seed, create random with no seed.
                    items[i] = new Random();
                    return i;//returns the index to say it was successful in opening/creating.
                }

            }
        }
        return -1;//returns -1 when there is no space to create this random device.
    }

    @Override
    public void close(int id) {
        items[id] = null;//null the value at id
    }

    @Override
    public byte[] read(int id, int size) {
        //grabs the random object at id.
        Random currRand = items[id];
        byte[] retVal = new byte[size];
        //loops through the byte array, populating it with currRand.
        for(int i = 0; i < size;i++){
            retVal[i] = (byte)currRand.nextInt();
        }
        return retVal;//returns the array.
    }

    @Override
    public void seek(int id, int to) {
        //grabs the random object at id.
        Random currRand = items[id];
        byte[] byteArray = new byte[to];
        //loops through the byte array, populating it with currRand.
        for(int i = 0; i < to;i++){
            byteArray[i] = (byte)currRand.nextInt();
        }
        //don't need to return array.
    }

    @Override
    public int write(int id, byte[] data) {
        //write does nothing so return 0.
        return 0;
    }
}
