package com.ly.io.netty.tcp.protocol;

/**
 * @Description 协议包
 * @Created by fun4ai
 * @Date 1/4 0004 16:01
 */
public class MessageProtocol {
    // 消息总长度
    private int length;
    private byte[] content;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
