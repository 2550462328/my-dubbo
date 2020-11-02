package com.zhanghui.support.constants;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * store the result of parser result
 *
 * @author: ZhangHui
 * @date: 2020/10/14 10:07
 * @version：1.0
 */
public class RpcParserResult {


    private final Set<RpcProvider> rpcProviders = ConcurrentHashMap.newKeySet();

    private final Set<RpcConsumer> rpcConsumers = ConcurrentHashMap.newKeySet();

    public void addRpcService(RpcProvider rpcProvider) {
        this.rpcProviders.add(rpcProvider);
    }

    public void addRpcConsumer(RpcConsumer rpcConsumer) {
        this.rpcConsumers.add(rpcConsumer);
    }

    public Iterator<RpcProvider> listRpcServices() {
        return this.rpcProviders.iterator();
    }

    public Iterator<RpcConsumer> listRpcConsumers() {
        return this.rpcConsumers.iterator();
    }
}
