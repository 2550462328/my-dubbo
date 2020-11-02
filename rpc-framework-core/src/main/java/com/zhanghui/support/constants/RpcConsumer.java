package com.zhanghui.support.constants;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author: ZhangHui
 * @date: 2020/10/14 9:30
 * @version：1.0
 */
@Setter
@Getter
@Builder
public class RpcConsumer {

    private String id;

    private String interfaceName;

    private String version;

    private String group;

}
