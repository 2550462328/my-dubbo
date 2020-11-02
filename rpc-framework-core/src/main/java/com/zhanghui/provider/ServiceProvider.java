package com.zhanghui.provider;

import com.zhanghui.entity.RpcServiceProperties;

/**
 * @ClassName ServiceProvider
 * @Description: provide service
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
public interface ServiceProvider {

    /**
     * @param service              service object
     * @param rpcServiceProperties service related attributes
     */
    void addService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     * @param rpcServiceProperties service related attributes
     * @return service object
     */
    Object getService(RpcServiceProperties rpcServiceProperties);

    /**
     * @param service              service object
     * @param rpcServiceProperties service related attributes
     */
    void publishService(Object service, RpcServiceProperties rpcServiceProperties);

    /**
     * @param service service object
     */
    void publishService(Object service);
}
