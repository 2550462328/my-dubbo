package zhanghui.project.helloclient;

import com.zhanghui.annotation.RpcScan;
import com.zhanghui.support.RpcBeanDefinitionReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@RpcScan(basePackage = {"zhanghui.project.helloclient"})
@SpringBootApplication
@ImportResource(locations = "classpath:dubbo-consumer.xml", reader = RpcBeanDefinitionReader.class)
public class HelloClientApplication implements CommandLineRunner{

    private String applicationName;

    public static void main(String[] args) {
        SpringApplication.run(HelloClientApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        StartUpRpcContext defaultRpcContext = new DefaultRpcContext();
//        Resource resource = new ClassPathResource("dubbo-consumer.xml",HelloClientApplication.class.getClassLoader());
//        defaultRpcContext.init(new Resource[]{resource});
    }
}
