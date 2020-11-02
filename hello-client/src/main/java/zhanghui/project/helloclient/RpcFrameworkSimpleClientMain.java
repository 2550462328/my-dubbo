package zhanghui.project.helloclient;


import com.zhanghui.api.Hello;
import com.zhanghui.api.HelloService;
import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.proxy.RpcClientProxy;
import com.zhanghui.remoting.ClientTransport;
import com.zhanghui.remoting.transport.socket.SocketClientTransport;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class RpcFrameworkSimpleClientMain {
    public static void main(String[] args) {
        ClientTransport clientTransport = new SocketClientTransport();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(clientTransport, rpcServiceProperties);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String hello = helloService.hello(new Hello("111", "222"));
        System.out.println(hello);
    }
}
