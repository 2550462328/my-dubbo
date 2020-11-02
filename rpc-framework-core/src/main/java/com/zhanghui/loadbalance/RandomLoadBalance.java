package com.zhanghui.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @ClassName RandomLoadBalance
 * @Description: random strategy
 * @Author: ZhangHui
 * @Date: 2020/10/10
 * @Version：1.0
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    public String doSelect(List<String> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
