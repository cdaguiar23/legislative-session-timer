package com.camara.model;

import java.io.Serializable;

public class SessionConfig implements Serializable {
    private String brasaoPath;
    private String controllerIp;
    private int controllerPort;
    private String controllerHost;
    private String controllerMac;
    private String broadcastAddress;
    private int broadcastPort;
    private String pautaPath;

    public SessionConfig() {
    }

    public SessionConfig(String brasaoPath) {
        this.brasaoPath = brasaoPath;
    }

    public String getBrasaoPath() {
        return brasaoPath;
    }

    public void setBrasaoPath(String brasaoPath) {
        this.brasaoPath = brasaoPath;
    }

    public String getControllerIp() {
        return controllerIp;
    }

    public void setControllerIp(String controllerIp) {
        this.controllerIp = controllerIp;
    }

    public int getControllerPort() {
        return controllerPort;
    }

    public void setControllerPort(int controllerPort) {
        this.controllerPort = controllerPort;
    }

    public String getControllerHost() {
        return controllerHost;
    }

    public void setControllerHost(String controllerHost) {
        this.controllerHost = controllerHost;
    }

    public String getControllerMac() {
        return controllerMac;
    }

    public void setControllerMac(String controllerMac) {
        this.controllerMac = controllerMac;
    }

    public String getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public int getBroadcastPort() {
        return broadcastPort;
    }

    public void setBroadcastPort(int broadcastPort) {
        this.broadcastPort = broadcastPort;
    }

    public String getPautaPath() {
        return pautaPath;
    }

    public void setPautaPath(String pautaPath) {
        this.pautaPath = pautaPath;
    }
}
