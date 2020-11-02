package com.zhanghui.remoting.transport.netty.client;

import com.zhanghui.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName UnProcessedRequest
 * @Description: save the request which not be response by server
 * @Author: ZhangHui
 * @Date: 2020/10/12 11:53
 * @Version：1.0
 */
public class UnProcessedRequest {

    private static final Map<String, CompletableFuture<RpcResponse>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    public void complete(RpcResponse response) {
        CompletableFuture future = UNPROCESSED_RESPONSE_FUTURES.remove(response.getRequestId());

        if (future != null) {
            future.complete(response);
        } else {
            throw new IllegalStateException();
        }
    }
}
