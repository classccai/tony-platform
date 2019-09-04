package com.tonytaotao.rpc.netty;

import com.tonytaotao.rpc.common.URL;

import java.net.InetSocketAddress;

public interface Endpoint {

    /**
     * get local socket address.
     *
     * @return local address.
     */
    InetSocketAddress getLocalAddress();

    /**
     * get remote socket address
     *
     * @return
     */
    InetSocketAddress getRemoteAddress();

    boolean open();

    boolean isAvailable();

    boolean isClosed();

    URL getUrl();

    void close();

    /**
     * close the channel gracefully.
     */
    void close(int timeout);
}