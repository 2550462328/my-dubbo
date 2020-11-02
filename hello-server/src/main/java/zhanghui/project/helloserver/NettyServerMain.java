package zhanghui.project.helloserver;

import com.zhanghui.annotation.RpcScan;
import com.zhanghui.api.HelloService;
import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.remoting.transport.netty.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zhanghui.project.helloserver.service.HelloServiceImpl2;

/**
 * Server: Automatic registration service via @RpcService annotation
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
@RpcScan(basePackage = {"com.zhanghui.service"})
public class NettyServerMain {

    public static void main(String[] args) {
        // Register service via annotation
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = (NettyServer) applicationContext.getBean("nettyServer");

        // Register service manually
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        nettyServer.registerService(helloService2, rpcServiceProperties);

        nettyServer.start();
    }
}
