import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Printer implements Runnable {
    private Lock lock;
    private Condition condition;
    private int flag = 0;
    public Printer(){
        lock = new ReentrantLock();
        condition = lock.newCondition();
        new Thread(this).start();
    }

    private void childThread() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            lock.lock();
            while (flag == 0){
                condition.await();
            }
            flag = 0;
            System.out.println("Child " + Thread.currentThread().getName() + " " + i);
            condition.signalAll();
            lock.unlock();

        }
    }

    public void mainThread() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            lock.lock();
            while (flag == 1){
                condition.await();
            }
            flag = 1;
            System.out.println("Main " + Thread.currentThread().getName() + " " + i);
            condition.signalAll();
            lock.unlock();

        }
    }

    @Override
    public void run() {
        try {
            childThread();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
