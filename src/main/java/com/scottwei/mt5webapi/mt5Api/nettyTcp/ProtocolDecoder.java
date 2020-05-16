
package com.scottwei.mt5webapi.mt5Api.nettyTcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;



/**
 * @author Scott Wei
 * @date 2019/7/30 10:16
 **/

/**
 * <pre>
 * **************************************************************************************************
 *                                          Protocol
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 *         4     │         4       │      1      │
 *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 *               │                 │             │
 *  │  Body Size     serial number      flag                     Body Content                      │
 *               │                 │             │
 *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 *
 * 9-byte header, example: LLLLKKKKF
 * + 4 // LLLL,  body size can be specified as a number in the range 0000-FFFF.
 * + 4 // KKKK, serial number must be within the range 0000-FFFF.
 * + 8 // F,  flag can be specified as a number in the range 0-F.
 * </pre>

 */
public class ProtocolDecoder extends ByteToMessageDecoder {

    public static final int HEAD_LENGTH = 9;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < HEAD_LENGTH) {
            return;
        }
        in.markReaderIndex();
        byte[] head = new byte[HEAD_LENGTH];
        in.readBytes(head);
        StringBuilder stringBuilder = new StringBuilder(HEAD_LENGTH);
        for (byte b : head) {
            stringBuilder.append((char) b);
        }
        String hexString = stringBuilder.toString();
        int bodySize = Integer.parseInt(hexString.substring(0, 4), 16);
        if (in.readableBytes() < bodySize) {
            in.resetReaderIndex();
            return;
        }
        Message msg = new Message();
        msg.setBodySize(bodySize);
        msg.setSerialNumber(Integer.parseInt(hexString.substring(4, 8), 16));
        msg.setFlag(Integer.parseInt(hexString.substring(8), 16));
        byte[] body = new byte[bodySize];
        in.readBytes(body);
        msg.setBody(new String(body, StandardCharsets.UTF_16LE));
        out.add(msg);
    }
}

