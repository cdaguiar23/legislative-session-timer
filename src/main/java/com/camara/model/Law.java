package com.camara.model;

public class Law {
    private String number;
    private String title;
    private String summary;
    private String author;
    private String section; // "EXPEDIENTE" or "ORDEM DO DIA"

    public Law(String number, String title, String summary) {
        this(number, title, summary, "", "ORDEM DO DIA");
    }

    public Law(String number, String title, String summary, String author, String section) {
        this.number = number;
        this.title = title;
        this.summary = summary;
        this.author = author;
        this.section = section;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public String toString() {
        if ("SECTION_HEADER".equals(number)) return title;
        if ("00".equals(number)) return title;
        return "Nº " + number;
    }
}
