package com.xxx.rpc.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 服务器端心跳检测<br/>
 *
 * @author pengc
 * @see com.xxx.rpc.server
 * @since 2019/9/10
 */
public class ServerHeartbeatHandler extends ChannelInboundHandlerAdapter {

    /**
     * 心跳丢失计数器
     */
    private int counter = 0;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Client is active ---");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("--- Client is inactive ---");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断接收到的包类型
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 空闲6s之后触发 (心跳包丢失)
            if (event.state() == IdleState.READER_IDLE) {
                if (counter >= 3) {
                    // 连续丢失3个心跳包 (断开连接)
                    ctx.channel().close();
                    System.out.println("关闭这个不活跃的channel");
                } else {
                    counter++;
                    System.out.println("丢失了第 " + counter + " 个心跳包");
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("连接出现异常");
        super.exceptionCaught(ctx, cause);
    }

}
