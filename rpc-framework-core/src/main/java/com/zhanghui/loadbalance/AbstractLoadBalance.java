package com.zhanghui.loadbalance;

import java.util.List;

/**
 * @ClassName AbstractLoadBalance
 * @Description: template for LoadBalance
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public String selectServiceAddress(List<String> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }

        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    public abstract String doSelect(List<String> serviceAddresses);
}
