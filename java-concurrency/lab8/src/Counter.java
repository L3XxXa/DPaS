import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Counter implements Runnable{
    private int threadCount;
    private int threadNum;
    private double result;
    private boolean runFlag;
    private CyclicBarrier cyclicBarrier;
    int i = 0;
    Counter(int threadCount, int threadNum, CyclicBarrier cyclicBarrier){
        this.threadCount = threadCount;
        this.threadNum = threadNum;
        result = 0.0;
        runFlag = true;
        this.cyclicBarrier = cyclicBarrier;
    }
    @Override
    public void run() {
        int iterator = threadNum;
        while(runFlag){
            result += Math.pow((-1.0), iterator) / (2 * iterator + 1);
            iterator+=threadCount;
            i++;
            if (i % 10 == 0){
                try {
                    System.out.println(Thread.currentThread().getName() + " " + i);
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }
        /*try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } */

        System.out.println("ENDING: " + Thread.currentThread().getName() + " " + i);

    }

    public void stop(){
        runFlag = false;
    }

    public double getResult() {
        System.out.println(i);
        return result;
    }
}
