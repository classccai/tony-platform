package com.tonytaotao.rpc.support;

public class ProtocolConfig extends XmlAbstractBeanConfig {

    private String name;

    private Integer port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
