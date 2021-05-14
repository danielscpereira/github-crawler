package br.com.danielscpereira.crawler.services;

import br.com.danielscpereira.crawler.model.GroupFile;
import java.util.List;
import br.com.danielscpereira.crawler.error.BusinessException;
import br.com.danielscpereira.crawler.model.FileItem;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Autowired
    private UrlService urlService;

    private Collection<Callable<Void>> tasks;
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public List<GroupFile> start(String url) {
        List<GroupFile> result = new ArrayList<>();
        try {

            ExecutorService executor = Executors.newFixedThreadPool(10);
            tasks = new ArrayList<>();

            List<FileItem> lista = Collections.synchronizedList(new ArrayList<>());

            if (!urlService.isUrlConnected(url)) {
                throw new BusinessException(String.format("Not connect url:%s", url));
            }

            String response = getResquest(url);
            extractInfo(response, lista);

            executor.invokeAll(tasks);

            result = lista.stream().collect(Collectors.groupingBy(FileItem::getExtension)).entrySet().stream().map(x -> {
                int sumAmount = x.getValue().stream().mapToInt(FileItem::getLines).sum();
                double sumPrice = x.getValue().stream().mapToDouble(FileItem::getFileSize).sum();
                return new GroupFile(x.getKey(), sumAmount, sumPrice);
            }).collect(toList());

            Collections.sort(result);

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException | IOException ex) {
            Logger.getLogger(CrawlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String getResquest(String url) throws InterruptedException, IOException {

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
                // add request header
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();

    }

    private Set<String> extractInfo(String body, List<FileItem> lista) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        Set<String> links = new HashSet<>();
        var patternLine = Pattern.compile("<span.+<a class=\\\"js-navigation.+href.+</a></span>");
        var patternLink = Pattern.compile("\"/.+\"");

        var matcher = patternLine.matcher(body);

        String link;
        while (matcher.find()) {
            var matcherLink = patternLink.matcher(matcher.group());

            if (matcherLink.find()) {
                link = matcherLink.group().replace("\"", "");
                if (link.contains("><span")) {
                    int indexOf = link.indexOf("><span");
                    link = link.substring(0, indexOf);
                }
                var url = String.format("https://github.com%s", link);
                if (isTree(url)) {
                    String response = getResquest(url);
                    Set<String> extractLinks = extractInfo(response, lista);
                    links.addAll(extractLinks);
                } else {

                    Callable<Void> task = () -> {
                        var fileItem = extractFileInfoPage(url);
                        lista.add(fileItem);
                        return null;
                    };
                    tasks.add(task);

                }

            }

        }

        return links;
    }

    public void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private boolean isTree(String url) {
        return url.contains("tree");
    }

    public FileItem extractFileInfoPage(String url) {
        try {
            FileItem fileItem = null;

            String body = getResquest(url);
            var patternInfoSizeFile = Pattern.compile("\\d[. 0-9]+(Bytes|KB|MB|GB)");
            var matcherInfoSizeFile = patternInfoSizeFile.matcher(body);

            var patternInfoLinesFile = Pattern.compile("\\d+ lines");
            var matcherInfoLinesFile = patternInfoLinesFile.matcher(body);

            if (matcherInfoSizeFile.find()) {
                var infoSize = matcherInfoSizeFile.group();
                String[] file = url.split("/");
                var name = file[file.length - 1];
                var lines = 0;

                if (matcherInfoLinesFile.find()) {
                    var infoLines = matcherInfoLinesFile.group();
                    String[] splitLinesInfo = infoLines.split(" ");
                    lines = Integer.parseInt(splitLinesInfo[0]);
                } else {
                    lines = extractLinesCompiledFiles(url);
                }

                double size = toBytes(infoSize);

                fileItem = new FileItem(name, size, lines);

            }

            return fileItem;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            Logger.getLogger(CrawlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private double toBytes(String sizeInfo) {
        String[] splitSizeInfo = sizeInfo.split(" ");

        String size = splitSizeInfo[0];
        String unit = splitSizeInfo[1].toUpperCase();

        switch (unit) {
            case "BYTES":
                return Double.parseDouble(size);
            case "KB":
                return Double.parseDouble(size) * 1000;
            case "MB":
                return Double.parseDouble(size) * 100000;
            case "GB":
                return Double.parseDouble(size) * 1000000000;
            default:
                return 0;

        }

    }

    private int extractLinesCompiledFiles(String url) throws InterruptedException, IOException {

        String body = getResquest(url);
        var patternLink = Pattern.compile("<a.+Download</a>");
        var matcherLink = patternLink.matcher(body);

        if (matcherLink.find()) {
            var resource = extractRawLink(matcherLink.group());
            String x = getResquest(resource);
            return (int) x.lines().count();
        }

        return 0;
    }

    public String extractRawLink(String body) {

        var patternLink = Pattern.compile("([\"'])(.*?)\\1");

        var matcher = patternLink.matcher(body);

        var link = "https://raw.githubusercontent.com";
        if (matcher.find()) {
            link += matcher.group(2).replace("/raw/", "/");
        }

        return link;
    }

}
