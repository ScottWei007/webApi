
package com.scottwei.mt5webapi.mt5Api.nettyTcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;


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


/**
 * @author Scott Wei
 * @date 2019/7/30 10:16
 **/
@ChannelHandler.Sharable
public class ProtocolEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] body = msg.getBody().getBytes(StandardCharsets.UTF_16LE);
        String bodySize = String.format("%04X", body.length);
        String serialNumber = String.format("%04X", msg.getSerialNumber());
        String flag = String.format("%01X", msg.getFlag());
        out.writeBytes((bodySize + serialNumber + flag).getBytes(StandardCharsets.UTF_8));
        out.writeBytes(body);
    }

}
