public class Main{
    public static void main(String[] args) throws Exception {
        OS.Startup(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld());
    }
}