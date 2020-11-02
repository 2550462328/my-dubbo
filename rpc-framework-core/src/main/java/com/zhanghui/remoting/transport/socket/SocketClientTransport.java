package com.zhanghui.remoting.transport.socket;

import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.exception.RpcException;
import com.zhanghui.extension.ExtensionLoader;
import com.zhanghui.registry.ServiceDiscovery;
import com.zhanghui.remoting.ClientTransport;
import com.zhanghui.remoting.dto.RpcRequest;
import com.zhanghui.remoting.dto.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @ClassName SocketClient
 * @Description: initialize and close the connection of socket server
 * @Author: ZhangHui
 * @Date: 2020/10/12 15:30
 * @Version：1.0
 */
public class SocketClientTransport implements ClientTransport {

    private final ServiceDiscovery serviceDiscovery;

    public SocketClientTransport() {
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build rpc service name by rpcRequest
        String rpcServiceName = RpcServiceProperties.builder()
                .serviceName(rpcRequest.getInterfaceName())
                .group(rpcRequest.getGroup())
                .version(rpcRequest.getVersion())
                .build().toRpcServiceName();
        InetSocketAddress socketAddress = serviceDiscovery.lookupService(rpcServiceName);
        try (Socket socket = new Socket()) {
            socket.connect(socketAddress);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(rpcRequest);
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            return inputStream.readObject();

        } catch (IOException | ClassNotFoundException ex) {
            throw new RpcException("调用服务失败:", ex);
        }
    }
}
