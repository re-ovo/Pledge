package dev.thomazz.pledge.pinger.frame.data;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class FrameData {
    private final Queue<Frame> expectingFrames = new ConcurrentLinkedQueue<>();
    private final AtomicReference<Frame> currentFrame = new AtomicReference<>();

    public boolean hasFrame() {
        return this.currentFrame.get() != null;
    }

    public void setFrame(Frame frame) {
        this.currentFrame.set(frame);
    }

    public Frame getFrame() {
        return this.currentFrame.get();
    }

    public Optional<Frame> continueFrame() {
        Frame frame = this.currentFrame.getAndSet(null);

        if (frame != null) {
            this.expectingFrames.add(frame);
        }

        return Optional.ofNullable(frame);
    }

    public Optional<Frame> matchStart(int id, Consumer<Frame> handler) {
        boolean exists = this.expectingFrames.stream().anyMatch(frame -> frame.getStartId() == id);
        if (!exists) return Optional.empty();
        for (Frame frame : this.expectingFrames) {
            handler.accept(frame);
            if (frame.getStartId() == id) {
                return Optional.of(frame);
            }
        }
        return Optional.empty();
    }

    public Optional<Frame> matchEnd(int id, Consumer<Frame> handler) {
        boolean exists = this.expectingFrames.stream().anyMatch(frame -> frame.getEndId() == id);
        if (!exists) return Optional.empty();
        for (Frame frame : this.expectingFrames) {
            handler.accept(frame);
            if (frame.getEndId() == id) {
                return Optional.of(frame);
            }
        }
        return Optional.empty();
    }

    public void popFrame(int id) {
        boolean exists = this.expectingFrames.stream().anyMatch(frame -> frame.getEndId() == id);
        if (!exists) return;
        do {
            Frame frame = this.expectingFrames.poll();
            if (frame.getEndId() == id) {
                return;
            }
        } while (true);
    }
}
