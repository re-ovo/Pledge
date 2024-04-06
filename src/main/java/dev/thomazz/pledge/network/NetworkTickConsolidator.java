package dev.thomazz.pledge.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.ArrayDeque;
import java.util.Queue;

public class NetworkTickConsolidator extends ChannelOutboundHandlerAdapter {
    private final Queue<NetworkMessage> messageQueue = new ArrayDeque<>();
    private volatile boolean open = false;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!this.open) {
            this.messageQueue.add(NetworkMessage.of(msg, promise));
            return;
        }

        super.write(ctx, msg, promise);
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public void drain(ChannelHandlerContext ctx) {
        while (!this.messageQueue.isEmpty()) {
            NetworkMessage message = this.messageQueue.poll();
            ctx.write(message.getMessage(), message.getPromise());
        }

        ctx.flush();
    }
}
