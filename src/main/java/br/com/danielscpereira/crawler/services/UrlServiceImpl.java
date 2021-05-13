package br.com.danielscpereira.crawler.services;

import br.com.danielscpereira.crawler.error.BusinessException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UrlServiceImpl implements UrlService {

    @Override
    public boolean isUrlConnected(String url) {
        try {
            var client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.discarding());

            return response.statusCode() == HttpStatus.OK.value();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Illegal character in path:" + url);
        } catch (IOException ex) {
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }

    }

}
