package com.zhanghui.registry.zk;

import com.zhanghui.enums.RpcErrorMessageEnum;
import com.zhanghui.exception.RpcException;
import com.zhanghui.loadbalance.LoadBalance;
import com.zhanghui.loadbalance.RandomLoadBalance;
import com.zhanghui.registry.ServiceDiscovery;
import com.zhanghui.registry.zk.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @ClassName
 * @Description: zk服务发现实现
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {

    private LoadBalance loadBalance;

    public ZkServiceDiscovery() {
        // default loadBalance
        this.loadBalance = new RandomLoadBalance();
    }

    public ZkServiceDiscovery(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {

        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);

        if (serviceUrlList.size() == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // load balancing
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList);

        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");

        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);

        return new InetSocketAddress(host, port);
    }
}
