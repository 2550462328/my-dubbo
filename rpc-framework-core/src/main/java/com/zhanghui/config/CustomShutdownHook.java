package com.zhanghui.config;

import com.zhanghui.registry.zk.util.CuratorUtils;
import com.zhanghui.utils.concurrent.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * When the server  is closed, do something such as unregister all services
 *
 * @author ZhangHui
 * @date 2020/10/10
 */

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry(CuratorUtils.getZkClient());
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
