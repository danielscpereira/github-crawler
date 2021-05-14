package br.com.danielscpereira.crawler.services;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface UrlService {

    boolean isUrlConnected(String url) throws InterruptedException, ExecutionException, TimeoutException, IOException;

}
