package com.zhanghui.support;

import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.enums.ParserErrorMessageEnum;
import com.zhanghui.enums.RpcErrorMessageEnum;
import com.zhanghui.exception.ResourceParserException;
import com.zhanghui.exception.RpcException;
import com.zhanghui.extension.ExtensionLoader;
import com.zhanghui.factory.SingletonFactory;
import com.zhanghui.provider.ServiceProvider;
import com.zhanghui.provider.ServiceProviderImpl;
import com.zhanghui.proxy.RpcClientProxy;
import com.zhanghui.remoting.ClientTransport;
import com.zhanghui.support.constants.RpcConsumer;
import com.zhanghui.support.constants.RpcParserResult;
import com.zhanghui.support.constants.RpcProvider;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * @author: ZhangHui
 * @date: 2020/10/15 10:36
 * @version：1.0
 */
@Slf4j
public class RpcBeanDefinitionReader extends AbstractBeanDefinitionReader {

    private final RpcParserResult rpcParserResult = new RpcParserResult();

    private final ServiceProvider serviceProvider;

    private final ClientTransport clientTransport;

    private final BeanDefinitionRegistry registry;

    public static final String DUBBO_NAMESPACE_URI = "http://code.alibabatech.com/schema/dubbo";

    public static final String SERVICE_ELEMENT = "service";

    public static final String CONSUMER_ELEMENT = "reference";

    public static final String ID_ATTRIBUTE = "id";

    public static final String GROUP_ATTRIBUTE = "group";

    public static final String VERSION_ATTRIBUTE = "version";

    public static final String REF_ATTRIBUTE = "ref";

    public static final String INTERFACE_ATTRIBUTE = "interface";

    public RpcBeanDefinitionReader(BeanDefinitionRegistry registry){
        super(registry);
        this.registry = registry;
        this.serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
        this.clientTransport = ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension("nettyClientTransport");
    }

    @Override
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {

        int beanCount = registry.getBeanDefinitionCount();

        if (resource == null || !resource.exists()) {
            throw new RpcException(RpcErrorMessageEnum.INIT_RESOURCE_NOT_NULL);
        }

        try (InputStream is = resource.getInputStream()) {

            SAXReader reader = new SAXReader();
            Document document = reader.read(is);

            Element root = document.getRootElement();

            Iterator<Element> iterator = root.elements().iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                String namespaceUri = element.getNamespaceURI();
                if (isDubboNamespace(namespaceUri)) {
                    parseDubboElement(element);
                }
            }
        } catch (IOException | DocumentException ex) {
            log.error("occur exception when parse the resource [{}]", resource.getDescription());
        }

        this.doRegisterServce();
        this.doCreateProxy();

        log.info("has registred beanDefinition nums [{}])", registry.getBeanDefinitionCount() - beanCount);
        return registry.getBeanDefinitionCount() - beanCount;
    }

    private void parseDubboElement(Element ele) {
        if (SERVICE_ELEMENT.equals(ele.getName())) {
            String interfaceName = ele.attributeValue(INTERFACE_ATTRIBUTE);

            try {
                if(Class.forName(interfaceName) == null){
                    throw new ResourceParserException(ParserErrorMessageEnum.SERVICE_NOT_FOUND,interfaceName);
                }
            } catch (ClassNotFoundException e) {
                throw new ResourceParserException(ParserErrorMessageEnum.SERVICE_NOT_FOUND,interfaceName);
            }

            String ref = ele.attributeValue(REF_ATTRIBUTE);
            String group = ele.attributeValue(GROUP_ATTRIBUTE);
            String version = ele.attributeValue(VERSION_ATTRIBUTE);

            RpcProvider rpcProvider = RpcProvider.builder()
                    .interfaceName(interfaceName)
                    .group(group)
                    .version(version)
                    .ref(ref).build();

            this.rpcParserResult.addRpcService(rpcProvider);
        } else if (CONSUMER_ELEMENT.equals(ele.getName())) {
            String interfaceName = ele.attributeValue(INTERFACE_ATTRIBUTE);

            try {
                if(Class.forName(interfaceName) == null){
                    throw new ResourceParserException(ParserErrorMessageEnum.SERVICE_NOT_FOUND,interfaceName);
                }
            } catch (ClassNotFoundException e) {
                throw new ResourceParserException(ParserErrorMessageEnum.SERVICE_NOT_FOUND,interfaceName);
            }

            String id = ele.attributeValue(ID_ATTRIBUTE);
            String group = ele.attributeValue(GROUP_ATTRIBUTE);
            String version = ele.attributeValue(VERSION_ATTRIBUTE);

            RpcConsumer rpcConsumer = RpcConsumer.builder()
                    .interfaceName(interfaceName)
                    .group(group)
                    .version(version)
                    .id(id).build();

            this.rpcParserResult.addRpcConsumer(rpcConsumer);
        }
    }

    /**
     * 判断命名方式是不是dubbo
     */
    private boolean isDubboNamespace(String namespaceUri) {
        return (!StringUtils.hasLength(namespaceUri) || DUBBO_NAMESPACE_URI.equals(namespaceUri));
    }

    public void doRegisterServce() {
        Iterator<RpcProvider> iterator = this.rpcParserResult.listRpcServices();

        while (iterator.hasNext()) {
            RpcProvider rpcProvider = iterator.next();
            this.registerServie(rpcProvider);
        }
    }

    /**
     * 生成代理对象
     */
    public void doCreateProxy() {
        Iterator<RpcConsumer> iterator = this.rpcParserResult.listRpcConsumers();

        while (iterator.hasNext()) {
            RpcConsumer rpcConsumer = iterator.next();

            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .version(rpcConsumer.getVersion()).group(rpcConsumer.getGroup()).build();

            RpcClientProxy clientProxy = new RpcClientProxy(clientTransport, rpcServiceProperties);

            try {
                Class targetClass = Class.forName(rpcConsumer.getInterfaceName());

                Object proxyBean = clientProxy.getProxy(targetClass);

                ((DefaultListableBeanFactory)registry).registerSingleton(rpcConsumer.getId(),proxyBean);

            } catch (ClassNotFoundException e) {
                throw new ResourceParserException(ParserErrorMessageEnum.SERVICE_NOT_FOUND, rpcConsumer.getInterfaceName());
            }
        }
    }

    /**
     * 向zk注册服务
     */
    private void registerServie(RpcProvider rpcProvider) {

        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .version(rpcProvider.getVersion()).group(rpcProvider.getGroup())
                .serviceName(rpcProvider.getInterfaceName()).build();

        String refBeanName = rpcProvider.getRef();

        Object refBean = ((DefaultListableBeanFactory)registry).getBean(refBeanName);

        if (refBean == null) {
            log.error("can`t find bean with beanName [{}]", refBeanName);
            throw new ResourceParserException(ParserErrorMessageEnum.SERVICE_NO_REALIZE, refBeanName);
        }
        serviceProvider.publishService(refBean, rpcServiceProperties);
    }
}
