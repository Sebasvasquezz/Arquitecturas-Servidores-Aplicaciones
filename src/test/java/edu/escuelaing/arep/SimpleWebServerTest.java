package edu.escuelaing.arep;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleWebServerTest {

    private static ExecutorService executorService;
    private static final int NUM_REQUESTS = 10;
    private static final String SERVER_URL = "http://localhost:8080";

    @BeforeAll
    public static void setUp() throws IOException {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                SimpleWebServer.main(new String[]{});
            } catch (IOException | ReflectiveOperationException e) {
                e.printStackTrace();
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException, ExecutionException {
        ExecutorService testExecutor = Executors.newFixedThreadPool(NUM_REQUESTS);
        HttpClient client = HttpClient.newHttpClient();

        Callable<Boolean> requestTask = () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(SERVER_URL + "/index.html"))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Status Code: " + response.statusCode());
                String expectedContentSnippet = "<title>Aplicaciones Distribuidas</title>";
                return response.statusCode() == 200 && response.body().contains(expectedContentSnippet);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        };

        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < NUM_REQUESTS; i++) {
            futures.add(testExecutor.submit(requestTask));
        }

        for (Future<Boolean> future : futures) {
            assertTrue(future.get(), "All requests should be successful and return the expected content");
        }

        testExecutor.shutdown();
    }
        @Test
    public void testHelloServiceGET() throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(SERVER_URL + "/app/hello?name=sebas"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Hola, sebas"));
    }
    
    @Test
    public void testPiServiceGET() throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(SERVER_URL + "/app/pi?decimals=2"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Pi con 2 decimales: 3.14" ));
    }

    @Test
    public void testGetContentType() {
        ClientHandler handler = new ClientHandler(null);

        assertEquals("text/html", handler.getContentType("index.html"));
        assertEquals("text/css", handler.getContentType("styles.css"));
        assertEquals("application/javascript", handler.getContentType("app.js"));
        assertEquals("image/jpeg", handler.getContentType("image.jpg"));
        assertEquals("text/plain", handler.getContentType("file.txt"));
    }

    @Test
    public void testHandleFileNotFound() throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(SERVER_URL + "/nonexistentfile.html"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("File Not Found"));
    }
}
