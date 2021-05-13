package br.com.danielscpereira.crawler.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GroupFile implements Comparable< GroupFile> {

    @ApiModelProperty(position = 0)
    private String extension;
    @ApiModelProperty(position = 1)
    private long count;
    @ApiModelProperty(position = 2)
    private double bytes;

    public GroupFile() {
    }

    public GroupFile(String extension, long count, double bytes) {
        this.extension = extension;
        this.count = count;
        this.bytes = bytes;
    }

    @Override
    public int compareTo(GroupFile o) {
        return this.getExtension().compareTo(o.getExtension());
    }

}
