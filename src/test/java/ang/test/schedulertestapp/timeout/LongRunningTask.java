package ang.test.schedulertestapp.timeout;

import java.util.Date;

class LongRunningTask implements Runnable {
    private final String message;

    LongRunningTask(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        System.out.println(new Date() + " " + LongRunningTask.class + ": " + message);
        for (int i = 0; true; i++) {
            if(Thread.interrupted()) {
                System.out.println(
                        new Date()
                        + "Task interrupted"
                        +" on thread "+Thread.currentThread().getName());
                return;
            }
        }
    }
}