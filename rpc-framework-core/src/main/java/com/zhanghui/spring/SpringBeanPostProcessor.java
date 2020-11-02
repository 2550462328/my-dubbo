package com.zhanghui.spring;

import com.zhanghui.annotation.RpcReference;
import com.zhanghui.annotation.RpcService;
import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.exception.RpcException;
import com.zhanghui.extension.ExtensionLoader;
import com.zhanghui.factory.SingletonFactory;
import com.zhanghui.provider.ServiceProvider;
import com.zhanghui.provider.ServiceProviderImpl;
import com.zhanghui.proxy.RpcClientProxy;
import com.zhanghui.remoting.ClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * as for bean ,when produce service we need registry the service
 * and when consume service we need invoke the proxy class of specific service
 *
 * @author: ZhangHui
 * @date: 2020/10/12 16:00
 * @version：1.0
 * @see BeanPostProcessor
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final ClientTransport clientTransport;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        this.clientTransport = ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension("nettyClientTransport");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        // registry the service annotated by @RpcService
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);

            Class<?> serviceRelatedInterface = bean.getClass().getInterfaces()[0];

            String serviceName = serviceRelatedInterface.getCanonicalName();

            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .version(rpcService.version()).group(rpcService.group()).serviceName(serviceName).build();

            serviceProvider.publishService(bean, rpcServiceProperties);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class targetClass = bean.getClass();

        Field[] fields = targetClass.getDeclaredFields();

        for (Field field : fields) {

            // check the field which was annotated by @RpcReference
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                        .version(rpcReference.version()).group(rpcReference.group()).build();

                // aim to create proxy object for the bean which was annotated by @RpcReference
                RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport, rpcServiceProperties);
                Object serviceProxy = rpcClientProxy.getProxy(field.getType());

                // set the field accessible when it`s decorated by private
                field.setAccessible(true);

                try {
                    field.set(bean, serviceProxy);
                } catch (IllegalAccessException e) {
                    throw new RpcException(e.getMessage(), e.getCause());
                }
            }
        }
        return bean;
    }
}
