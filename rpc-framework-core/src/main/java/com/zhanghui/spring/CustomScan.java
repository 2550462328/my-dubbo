package com.zhanghui.spring;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

/**
 * @ClassName CustomScan
 * @Description: customize scan for package
 * @Author: ZhangHui
 * @Date: 2020/10/12 15:55
 * @Version：1.0
 */
public class CustomScan extends ClassPathBeanDefinitionScanner {

    public CustomScan(BeanDefinitionRegistry registry, Class<? extends Annotation> annoType) {
        super(registry);
        super.addIncludeFilter(new AnnotationTypeFilter(annoType));
    }

    @Override
    public int scan(String... basePackages) {
        return super.scan(basePackages);
    }
}
