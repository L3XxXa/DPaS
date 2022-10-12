import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Main{
    public static void main(String[] args) throws InterruptedException {
        int threadAmount = Integer.parseInt(args[0]);
        List<Counter> counters = new ArrayList<>();
        List<Thread> threadList = new ArrayList<>();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(Integer.parseInt(args[0]));
        for (int i = 0; i < threadAmount; i++) {
            counters.add(new Counter(threadAmount, i, cyclicBarrier));
            threadList.add(new Thread(counters.get(i)));
        }
        threadList.forEach(Thread::start);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("Going to stop the counters");
                for (int i = 0; i < threadAmount; i++) {
                    counters.get(i).stop();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                double result = 0;
                for (int i = 0; i < threadAmount; i++) {

                    result += counters.get(i).getResult();
                }
                System.out.println(result * 4);
            }
        });

    }


}
