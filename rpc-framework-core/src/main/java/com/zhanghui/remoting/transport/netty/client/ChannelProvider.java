package com.zhanghui.remoting.transport.netty.client;

import com.zhanghui.factory.SingletonFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName ChannelProvider
 * @Description: provide and cache channel for each rpcService
 * @Author: ZhangHui
 * @Date: 2020/10/12
 * @Version：1.0
 */
@Slf4j
public class ChannelProvider {

    private final Map<String, Channel> channelMap;
    private final NettyClient nettyClient;

    public ChannelProvider() {
        this.channelMap = new ConcurrentHashMap<>();
        this.nettyClient = SingletonFactory.getInstance(NettyClient.class);
    }


    public Channel get(InetSocketAddress inetSocketAddress) {

        String key = inetSocketAddress.toString();

        //determine whether there is a connection in cache map
        if (channelMap.containsKey(key)) {

            Channel cacheChannel = channelMap.get(key);
            // determine the cache connection is available
            if (cacheChannel != null && cacheChannel.isActive()) {
                return cacheChannel;
            } else {
                channelMap.remove(key);
            }
        }

        // return new connection
        Channel newChannel = nettyClient.doConnect(inetSocketAddress);

        channelMap.put(key, newChannel);

        return newChannel;
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();

        channelMap.remove(key);

        log.info("has remove [{}] connection from cache", key);
    }
}
