public class Main {
    public static void main(String[] args) throws InterruptedException {
        Printer printer = new Printer();
        for (int i = 0; i < 5; i++) {
            printer.mainThread();
        }
    }
}
