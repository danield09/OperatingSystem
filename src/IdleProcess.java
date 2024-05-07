//Idle Process, calls cooperate() and then sleeps
public class IdleProcess extends UserlandProcess{
    @Override
    public void main() {
        while(true){
            Sleep(50);
            cooperate();
        }
    }
}
