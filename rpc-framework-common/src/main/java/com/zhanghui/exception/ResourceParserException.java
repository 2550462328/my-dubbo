package com.zhanghui.exception;


import com.zhanghui.enums.ParserErrorMessageEnum;
import com.zhanghui.enums.RpcErrorMessageEnum;

/**
 * @author shuang.kou
 * @createTime 2020年05月12日 16:48:00
 */
public class ResourceParserException extends RuntimeException {
    public ResourceParserException(String message) {
        super(message);
    }

    public ResourceParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceParserException(ParserErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }

    public ResourceParserException(ParserErrorMessageEnum rpcErrorMessageEnum, String detail) {
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }
}
