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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.summingInt;
import static java.util.stream.Collectors.toList;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class CrawlerServiceImpl implements CrawlerService {

    @Autowired
    private UrlService urlService;

    // private List<FileItem> lista;
    private ExecutorService executor;
    private Collection<Callable<Void>> tasks;
    // private static final 

    @Override
    public List<GroupFile> start(String url) {

        try {

            long startProcessingCrawlerTime = System.currentTimeMillis();

            executor = Executors.newFixedThreadPool(10);
            tasks = new ArrayList<>();
            //lista = 
            List<FileItem> lista = Collections.synchronizedList(new ArrayList<>());

            UUID uuid = UUID.randomUUID();

            List<GroupFile> result = new ArrayList<>();

            if (!urlService.isUrlConnected(url)) {
                throw new BusinessException(String.format("Not connect url:%s", url));
            }

            System.out.println("------------->   " + url + " | " + uuid + "   <-------------");
            String response = getResquest(url);
            Set<String> extractLinks = extractInfo(response, lista);

            /*
            extractLinks.parallelStream().forEach(x -> {
                //var xx = String.format("https://github.com%s", x);
                System.out.println("Thread : " + Thread.currentThread().getName() + ", value: " + x);

            });
             */
            long startProcessingTime = System.currentTimeMillis();

            System.out.println("Start find files ....");
            executor.invokeAll(tasks);

            awaitTerminationAfterShutdown(executor);

            long totalProcessingTime = System.currentTimeMillis() - startProcessingTime;

            System.out.println("UUID:" + uuid + " -- Time:" + totalProcessingTime + " --- Count" + lista.size());

            // while (!httpClient.executor().isEmpty()) {
            //     System.out.println("tem...");
            //  }
            //Thread.sleep(30000);
            // System.out.println("Thread:" + Thread.currentThread() + "Count" + lista.size());
            //lista.forEach(x -> System.out.println(x));
            // System.out.println("Count" + lista.size());
            result = lista.stream().collect(Collectors.groupingBy(FileItem::getExtension)).entrySet().stream().map((x) -> {
                int sumAmount = x.getValue().stream().mapToInt(FileItem::getLines).sum();
                double sumPrice = x.getValue().stream().mapToDouble(FileItem::getFileSize).sum();
                return new GroupFile(x.getKey(), sumAmount, sumPrice);
            }).collect(toList());

            Collections.sort(result);


            /*
            lista.stream().collect(Collectors.groupingBy(FileItem::getExtension)).entrySet().stream()
                    .collect(Collectors.toMap(x -> {
                        int sumAmount = x.getValue().stream().mapToInt(FileItem::getLines).sum();
                        double sumPrice = x.getValue().stream().mapToDouble(FileItem::getFileSize).sum();
                        return new GroupFile(x.getKey(), sumAmount, sumPrice);
                    }, Map.Entry::getValue)).entrySet().stream().collect(toList());
            
             */

 /*
            //Map<BlogPostType, Integer> likesPerType = posts.stream().collect(groupingBy(BlogPost::getType, summingInt(BlogPost::getLikes)));
           lista.stream().collect(groupingBy(FileItem::getExtension)).entrySet().stream().map( x- > {
             int sumAmount = x.getValue().stream().mapToInt(FileItem::getLines).sum();
            int sumPrice = x.getValue().stream().mapToInt(FileItem::getLines).sum();
            return new GroupFile(x.getKey(), sumAmount, sumPrice);
        } , toList());
             */
 /*
                   .collect(   x -> {
               int sumAmount = x.getValue().stream().mapToInt(FileItem::getLines).sum();
            int sumPrice = x.getValue().stream().mapToInt(FileItem::getFileSize).sum();
            return new GroupFile(x.getKey(), sumAmount, sumPrice);
           } , toList());*/
            // result = lista.stream().collect(Collectors.groupingBy(FileItem::getExtension)).
            /*
            .entrySet().stream().collect(Collectors.toList(x -> {
            int sumAmount = x.getValue().stream().mapToInt(FileItem::getLines).sum();
            int sumPrice = x.getValue().stream().mapToInt(FileItem::getFileSize).sum();
            return new GroupFile(x.getKey(), sumAmount, sumPrice);
            }));
            Map<BlogPostType, Set<BlogPost>> postsPerType = posts.stream()
            .collect(groupingBy(BlogPost::getType, toSet()));
             
            List<Map.Entry<String, List<FileItem>>> collect1 = lista.stream().collect(groupingBy(FileItem::getExtension, toList())).entrySet().stream().collect(Collectors.toList(x -> {
                int sumAmount = x.getValue().stream().mapToInt(FileItem::getLines).sum();
                int sumPrice = x.getValue().stream().mapToInt(FileItem::getFileSize).sum();
                return new GroupFile(x.getKey(), sumAmount, sumPrice);
            }, toList()));
             */
            long totalProcessingCrawlerTime = System.currentTimeMillis() - startProcessingCrawlerTime;

            System.out.println("Total Time:" + totalProcessingCrawlerTime);
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException | IOException ex) {
            Logger.getLogger(CrawlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static String getResquest(String url) throws InterruptedException, ExecutionException, TimeoutException, IOException {

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

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
        // CompletableFuture<HttpResponse<String>> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // String result = response.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        //   return result;
    }

    private Set<String> extractInfo(String body, List<FileItem> lista) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        Set<String> links = new HashSet<>();
        Pattern patternLine = Pattern.compile("<span.+<a class=\\\"js-navigation.+href.+</a></span>");
        Pattern patternLink = Pattern.compile("\"/.+\"");

        Matcher matcher = patternLine.matcher(body);

        String link;
        while (matcher.find()) {
            Matcher matcherLink = patternLink.matcher(matcher.group());

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
                    /*
                    links.add(url);
                    FileItem fileItem = extractFileInfoPage(url);
                    lista.add(fileItem);*/

                    Callable<Void> task = () -> {
                        links.add(url);
                        FileItem fileItem = extractFileInfoPage(url);
                        lista.add(fileItem);
                        // System.out.println("Thread" + Thread.currentThread());
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

    private static boolean isTree(String url) {
        return url.contains("tree");
    }

    public static FileItem extractFileInfoPage(String url) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        FileItem fileItem = null;

        String body = getResquest(url);
        Pattern patternInfoSizeFile = Pattern.compile("\\d+\\sBytes|[0-9]+([\\\\,\\\\.0-9]+)\\sKB+|[0-9]+([\\\\,\\\\.0-9]+)\\sMB|[0-9]+([\\\\,\\\\.0-9]+)\\sGB");
        Matcher matcherInfoSizeFile = patternInfoSizeFile.matcher(body);

        Pattern patternInfoLinesFile = Pattern.compile("\\d+ lines");
        Matcher matcherInfoLinesFile = patternInfoLinesFile.matcher(body);

        //case not extract lines from pages , the file is Compiled
        //when extract url from 
        if (matcherInfoSizeFile.find()) {
            var infoSize = matcherInfoSizeFile.group();
            String[] file = url.split("/");
            var name = file[file.length - 1];
            int lines = 0;

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

        //   System.out.println("------------------" + url + "------------------");
        //  System.out.println(fileItem);
        return fileItem;
    }

    private static double toBytes(String sizeInfo) {
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

    private static int extractLinesCompiledFiles(String url) throws InterruptedException, ExecutionException, TimeoutException, IOException {

        String body = getResquest(url);
        Pattern patternLink = Pattern.compile("<a.+Download</a>");
        Matcher matcherLink = patternLink.matcher(body);

        if (matcherLink.find()) {
            var resource = extractRawLink(matcherLink.group());
            String x = getResquest(resource);
            return (int) x.lines().count();
        }

        return 0;
    }

    public static String extractRawLink(String body) {

        Pattern patternLink = Pattern.compile("([\"'])(.*?)\\1");

        Matcher matcher = patternLink.matcher(body);

        String link = "https://raw.githubusercontent.com";
        if (matcher.find()) {
            link += matcher.group(2).replace("/raw/", "/");
        }

        return link;
    }

}
