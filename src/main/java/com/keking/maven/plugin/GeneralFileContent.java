package com.keking.maven.plugin;

public class GeneralFileContent {
    private String filename;
    private String content;
    private String encoding;

    public GeneralFileContent() {
    }

    public GeneralFileContent(String filename, String content) {
        this.filename = filename;
        this.content = content;
        this.encoding = "UTF-8";
    }

    public GeneralFileContent(String filename, String content, String encoding) {
        this.filename = filename;
        this.content = content;
        this.encoding = encoding;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
