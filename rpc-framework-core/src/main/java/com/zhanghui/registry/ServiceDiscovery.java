package com.zhanghui.registry;

import com.zhanghui.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @ClassName
 * @Description: 服务发现
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
@SPI
public interface ServiceDiscovery {

    /**
     * lookup service by rpcServiceName
     *
     * @param rpcServiceName rpc service name
     * @return service address
     */
    InetSocketAddress lookupService(String rpcServiceName);
}
