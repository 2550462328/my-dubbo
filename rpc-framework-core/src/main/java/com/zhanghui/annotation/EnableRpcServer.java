package com.zhanghui.annotation;

import com.zhanghui.enums.ServiceProviderEnum;
import com.zhanghui.spring.CustomStarterRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * whether open the rpcServer
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CustomStarterRegistrar.class)
public @interface EnableRpcServer {

    boolean isServer() default false;

    ServiceProviderEnum providerType() default ServiceProviderEnum.NETTY_SERVER;

}
