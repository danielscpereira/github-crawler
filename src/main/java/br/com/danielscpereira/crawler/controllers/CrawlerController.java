package br.com.danielscpereira.crawler.controllers;

import br.com.danielscpereira.crawler.error.ApiError;
import br.com.danielscpereira.crawler.model.GroupFile;
import br.com.danielscpereira.crawler.services.CrawlerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(maxAge = 3600)
@RestController
@Validated
@Api(tags = "Github Crawler Service")
public class CrawlerController {

    private static final String GITHUB_URL = "https://github.com/";

    @Autowired
    private CrawlerService crawlerService;

    @ApiOperation(value = "List of grouping files by extension and sum of their lines")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Item successfully recovered", response = GroupFile.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request", response = ApiError.class),
        @ApiResponse(code = 404, message = "The resource you were trying to access was not found", response = ApiError.class)
    }
    )

    @GetMapping(
            path = "/crawler",
            produces = "application/json")
    //produces = {"application/json", "application/xml"})
    public List<GroupFile> getFilesGroupedByExtension(
            @ApiParam(name = "repository", value = "Github repository not including the base https://github.com", defaultValue = "", required = true, example = "danielscpereira/test") @RequestParam String repository
            //,@ApiParam(name = "includeBranchs", value = "Including all branchs", defaultValue = "false", required = false, example = "false") @RequestParam(required = false) boolean includeBranchs
    ) {

        var url = GITHUB_URL + repository;

        return this.crawlerService.start(url);
    }

}
