package com.scottwei.mt5webapi.mt5Api.manager;

import com.scottwei.mt5webapi.mt5Api.exception.RemoteException;
import com.scottwei.mt5webapi.mt5Api.nettyTcp.Message;
import com.scottwei.mt5webapi.mt5Api.task.TimeoutTask;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Scott Wei
 * @date 2019/7/30 18:54
 **/
public class ClientManager {
    private static volatile ClientManager instance;
    private static final Lock lock = new ReentrantLock();

    public static ClientManager getInstance() {
        if(Objects.isNull(instance)) {
            try{
                lock.lock();
                if(Objects.isNull(instance)) {
                    instance = new ClientManager();
                }
            }finally {
                lock.unlock();
            }
        }
        return instance;
    }

    private ClientManager() {
    }

    private static final ConcurrentMap<Integer, CompletableFuture<Map<String,String>>> invokeCache = new ConcurrentHashMap<>();
    private static final HashedWheelTimer timeoutScanner = new HashedWheelTimer(new DefaultThreadFactory("mt5webapi.timeout.scanner",true), 50, TimeUnit.MILLISECONDS, 4096);
    private static final AtomicInteger sequence = new AtomicInteger(0);
    private static final int maxSequence = 0x3FFF;
    private static final long timeout = 3000;//invoke timeout
    private Channel channel;


    public boolean send(Message message) {
        if(Objects.nonNull(channel) && channel.isWritable()) {
            channel.writeAndFlush(message);
            return true;
        }
        return false;
    }

    public CompletableFuture<Map<String,String>> invoke(Message message) throws RemoteException {
        return this.invoke(message, timeout);
    }

    public CompletableFuture<Map<String,String>> invoke(Message message, long timeout) throws RemoteException {
        final int invokeId = getInvokeId();
        message.setSerialNumber(invokeId);
        message.setFlag(0);
        if(send(message)) {
            CompletableFuture<Map<String,String>> completableFuture = new CompletableFuture();
            invokeCache.put(invokeId, completableFuture);
            TimeoutTask timeoutTask = new TimeoutTask(invokeId, invokeCache);
            timeoutScanner.newTimeout(timeoutTask, timeout, TimeUnit.NANOSECONDS);
            return completableFuture;
        }else {
            throw new RemoteException("channel is unavailable");//不可发送
        }
    }

    public void received(Message message){
        CompletableFuture<Map<String,String>> completableFuture = invokeCache.remove(message.getSerialNumber());
        if(Objects.isNull(completableFuture)){
            return;
        }
        Map<String,String> params = parseToMap(message.getBody());
        if(params.get("RETCODE").startsWith("0")) {
            completableFuture.complete(params);
        }else {
            completableFuture.completeExceptionally(new RemoteException(params.toString()));//服务器异常
        }
    }

    private final int getInvokeId() {
         int invokeId = sequence.incrementAndGet();
         if(invokeId >= maxSequence){
             sequence.compareAndSet(maxSequence, 0);//reset sequence重设
             invokeId = sequence.incrementAndGet();
         }
        return invokeId;
    }


    private final Map<String,String> parseToMap(String str) {
        if(StringUtils.isEmpty(str)) {
            return  Collections.EMPTY_MAP;
        }
        Map<String,String> params = new HashMap<>();
        String[] array = str.split("\\|");
        for (int i = 0, length = array.length; i < length; i++) {
            if(i != 0 && i != length - 1) {
                String[] strs = array[i].split("=");
                params.put(strs[0], strs[1]);
            }
        }
        return params;
    }

    public void closeChannel() {
        if(Objects.nonNull(this.channel)){
            this.channel.close();
        }
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
