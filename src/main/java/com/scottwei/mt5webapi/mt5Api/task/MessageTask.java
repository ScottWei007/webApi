
package com.scottwei.mt5webapi.mt5Api.task;


import com.scottwei.mt5webapi.mt5Api.nettyTcp.Message;
import com.scottwei.mt5webapi.mt5Api.manager.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Scott Wei
 * @date 2019/7/30 16:37
 **/
public class MessageTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageTask.class);
    private final Message message;

    public MessageTask(Message message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            //handle business（处理业务）
            ClientManager.getInstance().received(message);
        } catch (Exception e) {
            logger.error(">>>>>>>>>MessageTask.run>>>>>>>>>error:{}", message.getSerialNumber());
        }
    }
}
