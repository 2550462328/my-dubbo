package com.zhanghui.remoting.transport.netty.server;

import com.zhanghui.enums.CompressTypeEnum;
import com.zhanghui.enums.RpcResponseCodeEnum;
import com.zhanghui.enums.SerializationTypeEnum;
import com.zhanghui.factory.SingletonFactory;
import com.zhanghui.remoting.constants.RpcConstants;
import com.zhanghui.remoting.dto.RpcMessage;
import com.zhanghui.remoting.dto.RpcRequest;
import com.zhanghui.remoting.dto.RpcResponse;
import com.zhanghui.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName MessageHandler
 * @Description: Customize the ChannelHandler of the server to process the data sent by the client.
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
@Slf4j
public class ServerMessageHandler extends ChannelInboundHandlerAdapter {

    private final RpcRequestHandler rpcRequestHandler;

    public ServerMessageHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof RpcMessage) {
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    RpcMessage rpcMessage = new RpcMessage();
                    rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                    rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    // Execute the target method (the method the client needs to execute) and return the method result
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        RpcMessage rpcMessage = new RpcMessage();
                        rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                        rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                        rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                        rpcMessage.setData(rpcResponse);
                        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        RpcMessage rpcMessage = new RpcMessage();
                        rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                        rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                        rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                        rpcMessage.setData(rpcResponse);
                        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        log.error("not writable now, message dropped");
                    }
                }
            }

        } finally {
            //Ensure that ByteBuf is released, otherwise there may be memory leaks
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
