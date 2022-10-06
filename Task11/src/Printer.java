import java.util.concurrent.Semaphore;

public class Printer implements Runnable {
    private Semaphore semaphore;
    private Semaphore semaphore1;
    private int countMain;
    private int countChild;
    public Printer(){
        semaphore = new Semaphore(1);
        semaphore1 = new Semaphore(0);
        countMain = 0;
        countChild = 0;
        new Thread(this).start();
    }

    private void childThread() throws InterruptedException {
        semaphore1.acquire();
        System.out.println("Child " + Thread.currentThread().getName() + " " + countChild);
        countChild++;
        semaphore.release();
    }

    public void mainThread() throws InterruptedException {
        semaphore.acquire();
        System.out.println("Main " + Thread.currentThread().getName() + " " + countMain);
        countMain++;
        semaphore1.release();
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            try {
                childThread();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
