package io.hifly.kafka.console.model;

public class ControllerNode {

    private Integer id;
    private String host;
    private Integer port;
    private boolean hasRack;
    private String rack;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean isHasRack() {
        return hasRack;
    }

    public void setHasRack(boolean hasRack) {
        this.hasRack = hasRack;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }
}
