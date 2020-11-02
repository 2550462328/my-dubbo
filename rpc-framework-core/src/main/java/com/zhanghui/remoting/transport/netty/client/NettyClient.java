package com.zhanghui.remoting.transport.netty.client;

import com.zhanghui.enums.RpcErrorMessageEnum;
import com.zhanghui.exception.RpcException;
import com.zhanghui.remoting.transport.netty.codec.RpcMessageDecoder;
import com.zhanghui.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName NettyClient
 * @Description: initialize and close the connect of netty server
 * @Author: ZhangHui
 * @Date: 2020/10/12
 * @Version：1.0
 */
@Slf4j
public class NettyClient {

    private final Bootstrap bootstrap;

    private final EventLoopGroup workersGroup;

    // initialize resources such as EventLoopGroup, Bootstrap
    public NettyClient() {

        workersGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(workersGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //  The timeout period of the connection.
                //  If this time is exceeded or the connection cannot be established, the connection fails.
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // If no data is sent to the server within 15 seconds, a heartbeat request is sent
                        pipeline.addLast(new IdleStateHandler(0, 15, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new ClientMessageHandler());
                    }
                });
    }

    /**
     * connect server and get the channel ,so that you can send rpc message to server
     *
     * @param socketAddress server address
     * @return the channel
     */
    @SneakyThrows
    public Channel doConnect(InetSocketAddress socketAddress) {

        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();

        bootstrap.connect(socketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("The client has connected [{}] successful", socketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                completableFuture.completeExceptionally(future.cause());
                throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE, "connect " + socketAddress.toString() + " failure");
            }
        });
        return completableFuture.get();
    }

    public void close() {
        workersGroup.shutdownGracefully();
    }
}
