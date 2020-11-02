package com.zhanghui.registry;

import com.zhanghui.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @ClassName
 * @Description: 注册服务
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
@SPI
public interface ServiceRegistry {

    /**
     * register service
     *
     * @param rpcServiceName    rpc service name
     * @param inetSocketAddress service address
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
