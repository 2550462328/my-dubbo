package com.zhanghui.remoting.transport.socket;

import com.zhanghui.config.CustomShutdownHook;
import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.factory.SingletonFactory;
import com.zhanghui.provider.ServiceProvider;
import com.zhanghui.provider.ServiceProviderImpl;
import com.zhanghui.remoting.transport.netty.server.NettyServer;
import com.zhanghui.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @ClassName SocketServer
 * @Description: socket server be ready for socket client to connnct
 * @Author: ZhangHui
 * @Date: 2020/10/12 15:06
 * @Version：1.0
 */
@Slf4j
public class SocketServer {

    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketServer() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent("rpc-socket-server-pool");
    }

    public void registerService(Object service) {
        serviceProvider.publishService(service);
    }

    public void registerService(Object service, RpcServiceProperties rpcServiceProperties) {
        serviceProvider.publishService(service, rpcServiceProperties);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {
            String host = InetAddress.getLocalHost().getHostAddress();
            serverSocket.bind(new InetSocketAddress(host, NettyServer.PORT));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = serverSocket.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRequestHanlderRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException ex) {
            log.error("occur Exception, ", ex);
        }
    }
}
