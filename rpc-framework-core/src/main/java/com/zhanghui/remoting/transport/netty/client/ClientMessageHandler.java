package com.zhanghui.remoting.transport.netty.client;

import com.zhanghui.enums.CompressTypeEnum;
import com.zhanghui.enums.SerializationTypeEnum;
import com.zhanghui.factory.SingletonFactory;
import com.zhanghui.remoting.constants.RpcConstants;
import com.zhanghui.remoting.dto.RpcMessage;
import com.zhanghui.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @ClassName ClientMessageHandler
 * @Description: Customize the client ChannelHandler to process the data sent by the server
 *
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，SimpleChannelInboundHandler 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 * @Author: ZhangHui
 * @Date: 2020/10/12
 * @Version：1.0
 */
@Slf4j
public class ClientMessageHandler extends ChannelInboundHandlerAdapter {

    private final UnProcessedRequest unProcessedRequest;
    private final ChannelProvider channelProvider;

    public ClientMessageHandler() {
        this.unProcessedRequest = SingletonFactory.getInstance(UnProcessedRequest.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage rpcMessage = (RpcMessage) msg;
                if (rpcMessage.getMessageType() == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", rpcMessage.getData());
                } else if (rpcMessage.getMessageType() == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse response = (RpcResponse) rpcMessage.getData();
                    unProcessedRequest.complete(response);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // send heartbeat package to server when client is in idle state (we set the time is 15 seconds)
        if (evt instanceof IdleStateEvent) {
            log.info("write idle happen [{}]", ctx.channel().remoteAddress());

            Channel channel = channelProvider.get((InetSocketAddress) ctx.channel().remoteAddress());

            RpcMessage rpcMessage = new RpcMessage();
            rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
            rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
            rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
            rpcMessage.setData(RpcConstants.PING);

            channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
