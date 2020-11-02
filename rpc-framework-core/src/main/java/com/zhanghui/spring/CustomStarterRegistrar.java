package com.zhanghui.spring;

import com.zhanghui.annotation.EnableRpcServer;
import com.zhanghui.enums.ServiceProviderEnum;
import com.zhanghui.remoting.transport.netty.server.NettyServer;
import com.zhanghui.remoting.transport.socket.SocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * scan and filter specified annotation
 *
 * @author: ZhangHui
 * @date: 2020/10/13 10:54
 * @version：1.0
 */
@Slf4j
public class CustomStarterRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String PROVIDER_TYPE  = "providerType";

    private static final String IS_SERVER = "isServer";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata classMetadata, BeanDefinitionRegistry registry) {

        // get the attributes and values of @RpcScan annotation
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(classMetadata.getAnnotationAttributes(EnableRpcServer.class.getName()));

        boolean isServer = annotationAttributes.getBoolean(IS_SERVER);

        ServiceProviderEnum providerEnum = annotationAttributes.getEnum(PROVIDER_TYPE);

        if(isServer){
            if(providerEnum == ServiceProviderEnum.NETTY_SERVER){
                new NettyServer().start();
            }else if(providerEnum == ServiceProviderEnum.SOCKET_SERVER){
                new SocketServer().start();
            }
        }
    }
}
