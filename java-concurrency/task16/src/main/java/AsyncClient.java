import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class AsyncClient {
    private final URI uri = URI.create("https://jsonplaceholder.typicode.com/comments");
    private final HttpClient httpClient;
    private final HttpRequest httpRequest;

    public AsyncClient() {
        this.httpClient = HttpClient.newBuilder().build();
        this.httpRequest = HttpRequest.newBuilder().GET().uri(uri).build();
    }

    public void makeRequest() {
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                .thenApply(HttpResponse::body)
                .thenAccept(stream -> {
                    AtomicInteger linesCount = new AtomicInteger();
                    stream.forEach(line -> {
                        System.out.println(line);
                        linesCount.getAndIncrement();

                        if (linesCount.get() % 25 == 0) {
                            try {
                                waitForSpace();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }).join();
    }

    private void waitForSpace() throws IOException {
        System.out.println("Press SPACE");
        int key;
        do {
            key = System.in.read();
        } while (key != ' ' && key != -1);
    }
}
