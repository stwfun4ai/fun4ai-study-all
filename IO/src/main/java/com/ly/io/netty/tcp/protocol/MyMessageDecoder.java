package com.ly.io.netty.tcp.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/4 0004 16:10
 */
public class MyMessageDecoder extends ReplayingDecoder<Void> {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("调用方法 MyMessageDecoder decode()");

        // 将二进制字节码转为对象
        int len = in.readInt();
        byte[] content = new byte[len];
        in.readBytes(content);
        // 封装对象放入list传入下一个handler
        MessageProtocol mp = new MessageProtocol();
        mp.setLength(len);
        mp.setContent(content);

        out.add(mp);
    }
}
