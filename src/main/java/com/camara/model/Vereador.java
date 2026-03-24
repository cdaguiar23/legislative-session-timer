package com.camara.model;

import java.io.Serializable;

public class Vereador implements Serializable {
    private String name;
    private String party;
    private String photoPath;
    private int microfoneId;

    public Vereador(String name, String party, String photoPath, int microfoneId) {
        this.name = name;
        this.party = party;
        this.photoPath = photoPath;
        this.microfoneId = microfoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public int getMicrofoneId() {
        return microfoneId;
    }

    public void setMicrofoneId(int microfoneId) {
        this.microfoneId = microfoneId;
    }
}
