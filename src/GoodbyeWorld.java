//Test Program, prints out "Goodbye World!", sleeps, and then cooperate()
public class GoodbyeWorld extends UserlandProcess{
    @Override
    public void main() {
        while(true){
            System.out.println("Goodbye World!");
            Sleep(50);
            cooperate();
        }
    }
}