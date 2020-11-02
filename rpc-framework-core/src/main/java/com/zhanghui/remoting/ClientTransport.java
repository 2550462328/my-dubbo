package com.zhanghui.remoting;

import com.zhanghui.extension.SPI;
import com.zhanghui.remoting.dto.RpcRequest;

/**
 * @ClassName ClientTransport
 * @Description: consumer send RpcRequest Message
 * @Author: ZhangHui
 * @Date: 2020/10/12
 * @Version：1.0
 */
@SPI
public interface ClientTransport {


    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
