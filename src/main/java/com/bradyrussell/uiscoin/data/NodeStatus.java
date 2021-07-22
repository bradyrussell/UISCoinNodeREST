package com.bradyrussell.uiscoin.data;

public class NodeStatus {
    private final boolean running;
    private final boolean connected;
    private final int blockheight;

    public NodeStatus(boolean running, boolean connected, int blockheight) {
        this.running = running;
        this.connected = connected;
        this.blockheight = blockheight;
    }

    public boolean getIsRunning() {
        return running;
    }

    public boolean getIsConnected() {
        return connected;
    }

    public int getBlockheight() {
        return blockheight;
    }
}
