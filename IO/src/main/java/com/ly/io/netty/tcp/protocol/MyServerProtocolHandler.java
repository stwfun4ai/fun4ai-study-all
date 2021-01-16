package com.ly.io.netty.tcp.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.util.UUID;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/4 0004 15:41
 */
public class MyServerProtocolHandler extends SimpleChannelInboundHandler<MessageProtocol> {
    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageProtocol mp) throws Exception {
        int length = mp.getLength();
        byte[] content = mp.getContent();


        System.out.println("服务端收到消息如下：");
        System.out.println("消息长度=" + length);
        System.out.println("消息内容=" + new String(content, CharsetUtil.UTF_8));

        System.out.println("服务端收到消息量：" + (++this.count));
        System.out.println("====================================");

        // 服务端回送消息给客户端UUID
        byte[] responseContent = UUID.randomUUID().toString().getBytes(CharsetUtil.UTF_8);
        int responseLen = responseContent.length;
        MessageProtocol response = new MessageProtocol();
        response.setContent(responseContent);
        response.setLength(responseLen);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
