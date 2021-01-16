package com.ly.io.netty.tcp.protocol;

import com.ly.io.netty.tcp.protocol.MessageProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * @Description
 * @Created by fun4ai
 * @Date 1/4 0004 15:33
 */
public class MyClientProtocolHandler extends SimpleChannelInboundHandler<MessageProtocol> {

    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol msg) throws Exception {
        int length = msg.getLength();
        byte[] content = msg.getContent();


        System.out.println("客户端收到消息如下：");
        System.out.println("消息长度=" + length);
        System.out.println("消息内容=" + new String(content, CharsetUtil.UTF_8));

        System.out.println("客户端收到消息量：" + (++this.count));
        System.out.println("====================================");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // 客户端发送10条数据
        for (int i = 0; i < 10; i++) {
            String mess = "今天天气冷，吃个火锅";
            byte[] content = mess.getBytes(CharsetUtil.UTF_8);
            int length = content.length;

            MessageProtocol mp = new MessageProtocol();
            mp.setLength(length);
            mp.setContent(content);

            ctx.writeAndFlush(mp);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
