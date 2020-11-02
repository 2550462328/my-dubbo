package com.zhanghui.provider;

import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.enums.RpcErrorMessageEnum;
import com.zhanghui.exception.RpcException;
import com.zhanghui.extension.ExtensionLoader;
import com.zhanghui.registry.ServiceRegistry;
import com.zhanghui.remoting.transport.netty.server.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName ServiceProviderImpl
 * @Description: default service provider implementor
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> registeredService;

    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl() {
        this.registeredService = new ConcurrentHashMap<>();

        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(Object service, RpcServiceProperties rpcServiceProperties) {
        String rpcServiceName = rpcServiceProperties.toRpcServiceName();
        if (registeredService.containsKey(rpcServiceName)) {
            return;
        }
        registeredService.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(RpcServiceProperties rpcServiceProperties) {
        Object service = registeredService.get(rpcServiceProperties.toRpcServiceName());
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(Object service) {
        this.publishService(service, RpcServiceProperties.builder().group("").version("").build());
    }

    @Override
    public void publishService(Object service, RpcServiceProperties rpcServiceProperties) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();

            this.addService(service, rpcServiceProperties);

            serviceRegistry.registerService(rpcServiceProperties.toRpcServiceName(), new InetSocketAddress(host, NettyServer.PORT));

        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
