package com.tonytaotao.rpc.protocol.defaults;

import com.tonytaotao.rpc.common.URL;
import com.tonytaotao.rpc.core.exporter.AbstractExporter;
import com.tonytaotao.rpc.core.exporter.Exporter;
import com.tonytaotao.rpc.core.message.MessageRouter;
import com.tonytaotao.rpc.core.provider.Provider;
import com.tonytaotao.rpc.core.reference.AbstractReference;
import com.tonytaotao.rpc.core.reference.Reference;
import com.tonytaotao.rpc.core.request.Request;
import com.tonytaotao.rpc.core.response.Response;
import com.tonytaotao.rpc.exception.FrameworkRpcException;
import com.tonytaotao.rpc.netty.client.NettyClient;
import com.tonytaotao.rpc.netty.client.NettyClientImpl;
import com.tonytaotao.rpc.netty.server.NettyServer;
import com.tonytaotao.rpc.netty.server.NettyServerImpl;
import com.tonytaotao.rpc.protocol.AbstractProtocol;
import com.tonytaotao.rpc.util.FrameworkUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRpcProtocol extends AbstractProtocol {

    private final ConcurrentHashMap<String, NettyServer> ipPort2Server = new ConcurrentHashMap<>();
    // 多个service可能在相同端口进行服务暴露，因此来自同个端口的请求需要进行路由以找到相应的服务，同时不在该端口暴露的服务不应该被找到
    private final Map<String, MessageRouter> ipPort2RequestRouter = new HashMap<>();

    @Override
    protected <T> Reference<T> createReference(Class<T> clz, URL url, URL serviceUrl) {
        return new DefaultRpcReference<>(clz, url, serviceUrl);
    }

    @Override
    protected <T> Exporter<T> createExporter(Provider<T> provider, URL url) {
        return new DefaultRpcExporter<>(provider, url);
    }

    @Override
    public void destroy() {

    }

    class DefaultRpcReference<T> extends AbstractReference<T> {
        private NettyClient client;

        DefaultRpcReference(Class<T> clz, URL url, URL serviceUrl) {
            super(clz, url, serviceUrl);
            this.client = new NettyClientImpl(serviceUrl);
        }

        @Override
        public Response doCall(Request request) {
            try {
                return client.invokeSync(request);
            } catch (Exception e) {
                throw new FrameworkRpcException("invoke exception", e);
            }
        }

        @Override
        public void init() {
            this.client.open();
        }

        @Override
        public void destroy() {
            try{
                client.close();
            } catch (Exception e){
                logger.error("reference destroy error", e);
            }
        }

        @Override
        public boolean isAvailable() {
            return client.isAvailable();
        }
    }

    class DefaultRpcExporter<T> extends AbstractExporter<T> {

        private NettyServer server;

        DefaultRpcExporter(Provider<T> provider, URL url) {
            super(provider, url);
            this.server = initServer(url);
        }

        private NettyServer initServer(URL url) {
            String ipPort = url.getServerAndPort();

            MessageRouter router = initRequestRouter(url);

            NettyServer server;
            synchronized (ipPort2Server) {
                server = ipPort2Server.get(ipPort);
                if (server == null) {
                    server = new NettyServerImpl(url, router);
                    ipPort2Server.put(ipPort, server);
                }
            }
            return server;
        }

        private MessageRouter initRequestRouter(URL url) {
            MessageRouter requestRouter;
            String ipPort = url.getServerAndPort();

            synchronized (ipPort2RequestRouter) {
                requestRouter = ipPort2RequestRouter.get(ipPort);

                if (requestRouter == null) {
                    requestRouter = new MessageRouter(provider);
                    ipPort2RequestRouter.put(ipPort, requestRouter);
                } else {
                    requestRouter.addProvider(provider);
                }
            }

            return requestRouter;
        }

        @Override
        public void unexport() {
            String protocolKey = FrameworkUtils.getProtocolKey(url);
            String ipPort = url.getServerAndPort();

            Exporter<T> exporter = (Exporter<T>) exporterMap.remove(protocolKey);

            if (exporter != null) {
                exporter.destroy();
            }

            synchronized (ipPort2RequestRouter) {
                MessageRouter requestRouter = ipPort2RequestRouter.get(ipPort);

                if (requestRouter != null) {
                    requestRouter.removeProvider(provider);
                }
            }

            logger.info("DefaultRpcExporter unexport success: url={}", url);
        }

        @Override
        public synchronized void init() {
            this.server.open();
        }

        @Override
        public synchronized void destroy() {
            this.server.close();
        }

        @Override
        public boolean isAvailable() {
            return this.server.isAvailable();
        }
    }
}