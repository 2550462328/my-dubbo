package com.zhanghui.proxy;

import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.enums.RpcErrorMessageEnum;
import com.zhanghui.enums.RpcResponseCodeEnum;
import com.zhanghui.exception.RpcException;
import com.zhanghui.remoting.ClientTransport;
import com.zhanghui.remoting.dto.RpcRequest;
import com.zhanghui.remoting.dto.RpcResponse;
import com.zhanghui.remoting.transport.netty.client.NettyClientTransport;
import com.zhanghui.remoting.transport.socket.SocketClientTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Dynamic proxy class.
 * When a dynamic proxy object calls a method, it actually calls the following invoke method.
 * It is precisely because of the dynamic proxy that the remote method called by the client is like calling the local method (the intermediate process is shielded)
 *
 * @author: ZhangHui
 * @date: 2020/10/12 16:25
 * @version：1.0
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final RpcServiceProperties rpcServiceProperties;
    private static final String INTERFACE_NAME = "interfaceName";

    /**
     * Used to send requests to the server.And there are two implementations: socket and netty
     */
    private final ClientTransport clientTransport;

    public RpcClientProxy(ClientTransport clientTransport, RpcServiceProperties rpcServiceProperties) {
        this.clientTransport = clientTransport;
        if (rpcServiceProperties.getGroup() == null) {
            rpcServiceProperties.setGroup("");
        }
        if (rpcServiceProperties.getVersion() == null) {
            rpcServiceProperties.setVersion("");
        }
        this.rpcServiceProperties = rpcServiceProperties;
    }


    public RpcClientProxy(ClientTransport clientTransport) {
        this.clientTransport = clientTransport;
        this.rpcServiceProperties = RpcServiceProperties.builder().group("").version("").build();
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * This method is actually called when you use a proxy object to call a method.
     * The proxy object is the object you get through the getProxy method.
     */
    @SneakyThrows
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceProperties.getGroup())
                .version(rpcServiceProperties.getVersion()).build();

        RpcResponse rpcResponse = null;

        if (clientTransport instanceof NettyClientTransport) {
            CompletableFuture<RpcResponse> completableFuture = ((NettyClientTransport) clientTransport).sendRpcRequest(rpcRequest);
            rpcResponse = completableFuture.get();
        } else if (clientTransport instanceof SocketClientTransport) {
            rpcResponse = (RpcResponse) clientTransport.sendRpcRequest(rpcRequest);
        }
        this.check(rpcResponse, rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse rpcResponse, RpcRequest rpcRequest) {
        if (rpcResponse == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (!rpcRequest.getRequestId().equals(rpcResponse.getRequestId())) {
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }

        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }
}
