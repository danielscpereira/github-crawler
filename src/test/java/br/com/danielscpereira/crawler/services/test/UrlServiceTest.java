package br.com.danielscpereira.crawler.services.test;

import br.com.danielscpereira.crawler.services.UrlService;
import br.com.danielscpereira.crawler.services.UrlServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UrlServiceTest {

    UrlService instance;

    @Before
    public void before() {
        this.instance = new UrlServiceImpl();
    }

    @Test
    public void whenConnectUrl() {
        boolean urlConnected = this.instance.isUrlConnected("https://github.com/danielscpereira/teste");
        Assert.assertTrue(urlConnected);
    }
    
    @Test
    public void whenNotConnectUrl() {
        boolean urlConnected = this.instance.isUrlConnected("https://github.com/danielscpereira/dasdasd");
        Assert.assertFalse(urlConnected);
    }
}
