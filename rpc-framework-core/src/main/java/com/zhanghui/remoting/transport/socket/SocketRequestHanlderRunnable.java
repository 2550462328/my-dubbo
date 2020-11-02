package com.zhanghui.remoting.transport.socket;

import com.zhanghui.factory.SingletonFactory;
import com.zhanghui.remoting.dto.RpcRequest;
import com.zhanghui.remoting.dto.RpcResponse;
import com.zhanghui.remoting.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @ClassName SocketRequestHanlderRunnable
 * @Description: 这是描述信息
 * @Author: ZhangHui
 * @Date: 2020/10/12 15:17
 * @Version：1.0
 */
@Slf4j
public class SocketRequestHanlderRunnable implements Runnable {

    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;

    public SocketRequestHanlderRunnable(Socket socket) {
        this.socket = socket;
        rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {

            RpcRequest rpcRequest = (RpcRequest) inputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            outputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            outputStream.flush();
        } catch (IOException | ClassNotFoundException ex) {
            log.error("occur exception:", ex);
        }
    }
}
