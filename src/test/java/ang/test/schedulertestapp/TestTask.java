package ang.test.schedulertestapp;

import java.util.Date;

public class TestTask  implements Runnable{
    private String message;

    public TestTask(String message){
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println(new Date()
                +" Runnable Task with "+ message
                +" on thread "+Thread.currentThread().getName());
    }
}
