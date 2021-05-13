package br.com.danielscpereira.crawler.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BusinessException extends RuntimeException {

    private final List<String> errors;

    public BusinessException() {
        this.errors = new ArrayList<>();
    }

    public BusinessException(String message) {
        super(message);
        this.errors = (Collections.singletonList(message));
    }

    public BusinessException(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

}
