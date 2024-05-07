//The Interface for Devices.
public interface Device {
    int open(String s) throws Exception;
    void close(int id) throws Exception;
    byte[] read(int id, int size) throws Exception;
    void seek(int id, int to) throws Exception;
    int write(int id, byte[] data) throws Exception;
}
