package zhanghui.project.helloclient.controller;

import com.zhanghui.annotation.RpcReference;
import com.zhanghui.api.Hello;
import com.zhanghui.api.HelloService;
import com.zhanghui.spring.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author: ZhangHui
 * @date: 2020/10/13 11:41
 * @version：1.0
 */
@Controller
public class HelloController {

//    @RpcReference(version = "version1", group = "test1")
    @Resource(name = "helloService")
    private HelloService helloService;

    @GetMapping("hello")
    @ResponseBody
    public void test(){
//        HelloService helloService = (HelloService) SpringContext.getBean("helloService");

        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222")));
        }
    }
}
