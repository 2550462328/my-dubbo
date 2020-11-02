package com.zhanghui.loadbalance;

import java.util.List;

/**
 * @ClassName LoadBalance
 * @Description: 负载均衡接口
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
public interface LoadBalance {

    /**
     * Choose one from the list of existing service addresses list
     *
     * @param serviceAddresses Service address list
     * @return target service address
     */
    String selectServiceAddress(List<String> serviceAddresses);
}
