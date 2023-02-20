import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String port = args[0];
        String translator = args[1];
        String ipToTranslate = args[2];
        Server server = new Server(port, translator, ipToTranslate);
    }
}
