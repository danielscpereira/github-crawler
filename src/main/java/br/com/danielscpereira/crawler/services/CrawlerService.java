package br.com.danielscpereira.crawler.services;

import br.com.danielscpereira.crawler.model.GroupFile;
import java.util.List;

public interface CrawlerService {

    List<GroupFile> start(String url) ;

}
