package com.scottwei.mt5webapi.mt5Api.task;

import com.scottwei.mt5webapi.mt5Api.exception.RemoteException;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Scott Wei
 * @date 2019/8/1 11:03
 **/
public class TimeoutTask implements TimerTask {
    private final long invokeId;
    private final ConcurrentMap<Integer, CompletableFuture<Map<String,String>>> invokeCache;

    public TimeoutTask(long invokeId, ConcurrentMap<Integer, CompletableFuture<Map<String, String>>> invokeCache) {
        this.invokeId = invokeId;
        this.invokeCache = invokeCache;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        CompletableFuture<Map<String,String>> completableFuture = invokeCache.remove(invokeId);
        if (Objects.nonNull(completableFuture)) {
            completableFuture.completeExceptionally(new RemoteException("invoke timeout"));//服务器超时
        }
    }
}
