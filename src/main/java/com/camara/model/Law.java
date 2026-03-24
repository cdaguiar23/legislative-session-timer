package com.camara.model;

public class Law {
    private String number;
    private String title;
    private String summary;

    public Law(String number, String title, String summary) {
        this.number = number;
        this.title = title;
        this.summary = summary;
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

    @Override
    public String toString() {
        if ("00".equals(number)) return title;
        return "Nº " + number;
    }
}
