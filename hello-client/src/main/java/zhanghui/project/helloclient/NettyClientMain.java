package zhanghui.project.helloclient;

import com.zhanghui.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zhanghui.project.helloclient.controller.HelloController;

/**
 * @author: ZhangHui
 * @date: 2020/10/13 11:42
 * @version：1.0
 */
@RpcScan(basePackage = {"com.zhanghui"})
public class NettyClientMain {

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController controller = (HelloController) context.getBean("helloController");
        controller.test();
    }

}
