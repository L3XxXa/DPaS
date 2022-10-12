public class Main {
    public static void main(String[] args) {
        System.out.println("Active thread " + Thread.activeCount());
        Thread thread = new Thread(()->{
            while(true){
                System.out.println("Hello, world");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        System.out.println("Active Thread " + Thread.activeCount());
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                System.out.println("We are goint to stop this thread");
                thread.interrupt();

            }
        });
        System.out.println("Active thread " + Thread.activeCount());
    }
}
