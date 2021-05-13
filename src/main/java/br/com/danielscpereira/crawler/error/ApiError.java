package br.com.danielscpereira.crawler.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.http.HttpStatus;
import lombok.Data;

@Data
public class ApiError {

    @ApiModelProperty(position = 0, example = "01/01/2020 13:22:45")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", locale = "pt-BR", timezone = "America/Sao_Paulo")
    private Date timestamp;
    @ApiModelProperty(position = 1, example = "400")
    private int status;
    @ApiModelProperty(position = 2, example = "[\"erro1\", \"erro2\", \"erro3\"]")
    private List<String> errors;

    public ApiError(HttpStatus status, List<String> errors) {
        super();
        this.status = status.value();
        this.timestamp = new Date();
        this.errors = errors;
    }

    public ApiError(HttpStatus status, String error) {
        super();
        this.timestamp = new Date();
        this.status = status.value();
        errors = Arrays.asList(error);
    }

}
