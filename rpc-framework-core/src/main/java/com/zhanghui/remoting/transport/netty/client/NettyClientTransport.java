package com.zhanghui.remoting.transport.netty.client;

import com.zhanghui.enums.CompressTypeEnum;
import com.zhanghui.enums.RpcErrorMessageEnum;
import com.zhanghui.enums.SerializationTypeEnum;
import com.zhanghui.exception.RpcException;
import com.zhanghui.extension.ExtensionLoader;
import com.zhanghui.factory.SingletonFactory;
import com.zhanghui.registry.ServiceDiscovery;
import com.zhanghui.remoting.ClientTransport;
import com.zhanghui.remoting.constants.RpcConstants;
import com.zhanghui.remoting.dto.RpcMessage;
import com.zhanghui.remoting.dto.RpcRequest;
import com.zhanghui.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * @ClassName NettyClientTransport
 * @Description: implements {@link com.zhanghui.remoting.ClientTransport} based on netty
 * @Author: ZhangHui
 * @Date: 2020/10/12
 * @Version：1.0
 */
@Slf4j
public class NettyClientTransport implements ClientTransport {

    private final ServiceDiscovery serviceDiscovery;
    private final ChannelProvider channelProvider;
    private final UnProcessedRequest unProcessedRequest;

    public NettyClientTransport() {
        serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        unProcessedRequest = SingletonFactory.getInstance(UnProcessedRequest.class);
    }

    @Override
    public CompletableFuture<RpcResponse> sendRpcRequest(RpcRequest rpcRequest) {

        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();

        unProcessedRequest.put(rpcRequest.getRequestId(), completableFuture);

        String rpcServiceName = rpcRequest.toRpcProperties().toRpcServiceName();

        InetSocketAddress inetAddress = serviceDiscovery.lookupService(rpcServiceName);

        Channel channel = channelProvider.get(inetAddress);

        if (channel != null && channel.isActive()) {
            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            rpcMessage.setData(rpcRequest);
            rpcMessage.setMessageType(RpcConstants.REQUEST_TYPE);

            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    completableFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
        }
        return completableFuture;
    }
}
