package com.zhanghui.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author shuang.kou
 * @createTime 2020年05月12日 16:45:00
 */
@AllArgsConstructor
@Getter
@ToString
public enum ParserErrorMessageEnum {

    SERVICE_NO_REALIZE("注册的服务没有找到实现"),

    SERVICE_NOT_FOUND("指定服务不存在");

    private final String message;

}
