//Test program that prints out "Hello World!", sleeps, and call cooperate()
public class HelloWorld extends UserlandProcess{
    @Override
    public void main(){
        while(true){
            System.out.println("Hello World!");
            Sleep(50);
            cooperate();
        }
    }
}