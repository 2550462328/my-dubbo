package zhanghui.project.helloserver;


import com.zhanghui.api.HelloService;
import com.zhanghui.entity.RpcServiceProperties;
import com.zhanghui.remoting.transport.socket.SocketServer;
import zhanghui.project.helloserver.service.HelloServiceImpl;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
public class RpcFrameworkSimpleServerMain {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        SocketServer socketRpcServer = new SocketServer();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        socketRpcServer.registerService(helloService, rpcServiceProperties);
        socketRpcServer.start();
    }
}
