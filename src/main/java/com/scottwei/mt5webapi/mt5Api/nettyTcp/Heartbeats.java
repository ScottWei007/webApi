
package com.scottwei.mt5webapi.mt5Api.nettyTcp;

import com.scottwei.mt5webapi.common.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;


/**
 * @author Scott Wei
 * @date 2019/7/30 10:16
 *
 *  heartbeat心跳包
 **/
public class Heartbeats {

    private static final ByteBuf HEARTBEAT_BUF;

    static {
        ByteBuf buf = Unpooled.buffer(ProtocolDecoder.HEAD_LENGTH);
        String bodySize = String.format("%04X", 0);//0000
        String serialNumber = String.format("%04X", 1);//0001
        String flag = String.format("%01X", 0);//0
        buf.writeBytes((Constant.PREFIX + bodySize + serialNumber + flag).getBytes(StandardCharsets.UTF_8));
        HEARTBEAT_BUF = Unpooled.unreleasableBuffer(buf).asReadOnly();
    }

    /**
     * Returns the shared heartbeat content.
     */
    public static ByteBuf heartbeatContent() {
        return HEARTBEAT_BUF.duplicate();
    }
}
