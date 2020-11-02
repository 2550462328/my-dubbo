package com.zhanghui.spring;

import com.zhanghui.annotation.RpcScan;
import com.zhanghui.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * scan and filter specified annotation
 *
 * @author: ZhangHui
 * @date: 2020/10/13 10:54
 * @version：1.0
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final String SPRING_BEAN_BASE_PACKAGE = "com.zhanghui.spring";

    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata classMetadata, BeanDefinitionRegistry registry) {

        // get the attributes and values of @RpcScan annotation
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(classMetadata.getAnnotationAttributes(RpcScan.class.getName()));

        String[] rpcScanPackages = new String[0];

        if (rpcScanAnnotationAttributes != null) {
            rpcScanPackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }

        // default scan packages
        if (rpcScanPackages.length == 0) {
            rpcScanPackages = new String[]{((StandardAnnotationMetadata) classMetadata).getIntrospectedClass().getPackage().getName()};
        }

        // customize scanner for scan the specified annotation
        CustomScan rpcServiceScanner = new CustomScan(registry, RpcService.class);
        CustomScan springBeanScanner = new CustomScan(registry, Component.class);

        // scan the class which was annotated by @RpcService
        int rpcServiceAnnoCount = rpcServiceScanner.scan(rpcScanPackages);
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceAnnoCount);

        // scan the SpringBeanPostProcessor to spring lifestyle
        int springBeanAnnoCount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAnnoCount);
    }
}
