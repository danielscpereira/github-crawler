package br.com.danielscpereira.crawler.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UrlServiceImpl implements UrlService {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public boolean isUrlConnected(String url) throws InterruptedException, ExecutionException, TimeoutException, IOException {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36")
                .setHeader("accept", "text/html")
                .setHeader("sec-fetch-site", "none")
                .setHeader("sec-fetch-mode", "navigate")
                .setHeader("sec-fetch-user", "?1")
                .setHeader("sec-fetch-dest", "document")
                .setHeader("dnt", "1")
                .setHeader("upgrade-insecure-requests", "1")
                .setHeader("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

        return response.statusCode() == HttpStatus.OK.value();

    }

}
