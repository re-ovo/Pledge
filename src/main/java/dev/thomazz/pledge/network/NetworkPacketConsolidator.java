package dev.thomazz.pledge.network;

import dev.thomazz.pledge.packet.PacketFiltering;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.ArrayDeque;
import java.util.Queue;

// Prevents asynchronously sent packets to land outside the start and end ping interval
public class NetworkPacketConsolidator extends ChannelOutboundHandlerAdapter {
    private final Queue<NetworkMessage> messageQueue = new ArrayDeque<>();
    private boolean started = false;
    private boolean open = true;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Start with login packet in game state
        if (PacketFiltering.isLoginPacket(msg)) {
            this.started = true;
        }

        // Check if started, some packets are whitelisted from being queued
        if (this.started && !this.open && !PacketFiltering.isWhitelistedFromQueue(msg)) {
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
