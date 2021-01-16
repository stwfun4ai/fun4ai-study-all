package com.ly.io.netty.tcp.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/4 0004 16:10
 */
public class MyMessageEncoder extends MessageToByteEncoder<MessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessageProtocol messageProtocol, ByteBuf out) throws Exception {
        System.out.println("调用方法 MyMessageEncoder encode()");

        out.writeInt(messageProtocol.getLength());
        out.writeBytes(messageProtocol.getContent());

    }
}
