package br.com.danielscpereira.crawler.controllers.test;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.containsString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CrawlerControllerAppIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void whenTestApp_thenBadRequestResponse() throws Exception {
        this.mvc.perform(get("/crawler")).andExpect(status().isBadRequest()).andExpect(content().string(containsString("repository parameter is missing")));
    }

    @Test
    public void whenTestApp_thenNotFoundRepositoryResponse() throws Exception {
        this.mvc.perform(
                get("/crawler")
                        .param("repository", "danielscpereira/test"))
                .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Not connect")));
    }
    
    
    @Test
    public void whenTestApp_thenInvalidPathRepositoryResponse() throws Exception {
        this.mvc.perform(
                get("/crawler")
                        .param("repository", "danielscpereira / t est"))
                .andExpect(status().isBadRequest()).andExpect(content().string(containsString("Illegal character")));
    }
     
    
}

