package ang.test.schedulertestapp;

import java.util.Date;

public class TestTask  implements Runnable{
    private final String message;
    private int counter = 0; // counter of run calls basically

    public TestTask(String message){
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println(new Date()
                +" Runnable Task with "+ message
                +" on thread "+Thread.currentThread().getName());
        counter++;
    }

    public int getCounter() {
        return counter;
    }
}
