package zhanghui.project.helloserver;

import com.zhanghui.annotation.EnableRpcServer;
import com.zhanghui.annotation.RpcScan;
import com.zhanghui.enums.ServiceProviderEnum;
import com.zhanghui.support.RpcBeanDefinitionReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@RpcScan(basePackage = {"zhanghui.project.helloserver"})
@SpringBootApplication
@ImportResource(locations = "classpath:dubbo-provider.xml", reader = RpcBeanDefinitionReader.class)
@EnableRpcServer(isServer = true,providerType = ServiceProviderEnum.NETTY_SERVER)
public class HelloServerApplication implements CommandLineRunner {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HelloServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        StartUpRpcContext defaultRpcContext = new DefaultRpcContext(new NettyServer());
//        Resource resource = new ClassPathResource("dubbo-provider.xml", HelloServerApplication.class.getClassLoader());
//        defaultRpcContext.init(new Resource[]{resource});
    }
}
