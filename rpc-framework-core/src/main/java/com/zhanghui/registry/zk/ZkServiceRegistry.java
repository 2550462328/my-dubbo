package com.zhanghui.registry.zk;

import com.zhanghui.registry.ServiceRegistry;
import com.zhanghui.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @ClassName
 * @Description: zk注册服务实现类
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String serviceNodePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();

        CuratorFramework zkClient = CuratorUtils.getZkClient();

        CuratorUtils.createPersistentNode(zkClient, serviceNodePath);
    }
}
