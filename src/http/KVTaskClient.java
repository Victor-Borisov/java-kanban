package http;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final String url;
    private final String apiKey;

    private HttpClient client = HttpClient.newHttpClient();
    private HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
    private HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private HttpRequest request;
    private HttpResponse<String> response;

    public KVTaskClient(String url) {
        this.url = url;
        this.apiKey = register();
    }

    public String register() {
        request = requestBuilder
                .uri(URI.create(url + "/register"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text/html")
                .GET()
                .build();
        try {
            response = client.send(request, handler);
            if (response.statusCode() != 200) {
                throw new RuntimeException();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException();
        }
        return response.body();
    }


    public void put(String key, String json) {
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        request = requestBuilder
                .uri(URI.create(url + "/save/" + key + "?API_TOKEN=" + apiKey))
                .POST(body)
                .build();
        try {
            response = client.send(request, handler);
            if (response.statusCode() != 200) {
                throw new RuntimeException();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException();
        }
    }

    public String load(String key) {
        request = requestBuilder
                .uri(URI.create(url + "/load/" + key + "?API_TOKEN=" + apiKey))
                .GET()
                .build();
        try {
            response = client.send(request, handler);
            if (response.statusCode() != 200) {
                throw new RuntimeException();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException();
        }
        return response.body();
    }
}