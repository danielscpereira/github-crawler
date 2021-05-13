package br.com.danielscpereira.crawler.model;

import lombok.Data;

@Data
public class FileItem {

    private String name;
    private double fileSize;
    private int lines;

    public FileItem(String name, double fileSize, int lines) {
        this.name = name;
        this.fileSize = fileSize;
        this.lines = lines;
    }

    public String getExtension() {

        var extension = "noExtension";

        if (this.name != null && !this.name.isBlank() && !this.name.startsWith(".") && this.name.contains(".")) {
            String[] x = this.name.split("\\.(?=[^\\.]+$)");
            if (x.length > 0) {
                extension = x[x.length - 1];
            }
        }

        return extension;
    }

    @Override
    public String toString() {
        return "Name:" + name + "\nSize:" + fileSize + "\nlines:" + lines;
    }
}
