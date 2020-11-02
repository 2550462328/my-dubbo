package com.zhanghui.registry.zk.util;

import com.zhanghui.enums.RpcConfigEnum;
import com.zhanghui.exception.RpcException;
import com.zhanghui.utils.file.PropertiesFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName CuratorUtils
 * @Description: curator工具类
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
@Slf4j
public final class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;

    // root path
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";

    // use for cache
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();

    // contain the all registry nodes
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();

    private static CuratorFramework zkClient;
    private static String defaultZookeeperAddress = "127.0.0.1:2181";

    private CuratorUtils() {
    }

    /**
     * Create persistent nodes. Unlike temporary nodes, persistent nodes are not removed when the client disconnects
     *
     * @param path node path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                //eg: /my-rpc/github.javaguide.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Gets the children under a node
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version1
     * @return All child nodes under the specified node
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;

        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * Registers to listen for changes to the specified node
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) {

        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;

        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);

        PathChildrenCacheListener pathChildrenCacheListener = (client, event) -> {
            List<String> serviceAddresses = client.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e.getCause());
        }
    }


    /**
     * Empty the registry of data
     */
    public static void clearRegistry(CuratorFramework zkClient) {

        REGISTERED_PATH_SET.stream().forEach(servicePath -> {

            try {
                zkClient.delete().forPath(servicePath);
            } catch (Exception e) {
                throw new RpcException(e.getMessage(), e.getCause());
            }
        });

        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }

    /**
     * get zkClient with start state
     * use default zk address when user don`t set it manual
     */
    public static CuratorFramework getZkClient() {

        // check if user has set zk address
        Properties properties = PropertiesFileUtils.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        if (properties != null) {
            defaultZookeeperAddress = properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue());
        }
        // if zkClient has been started, return directly
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }

        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);

        zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString(defaultZookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();

        return zkClient;
    }

}
