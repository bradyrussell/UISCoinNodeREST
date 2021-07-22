package com.bradyrussell.uiscoin.data;

import java.util.ArrayList;

public class PeerList {
    private ArrayList<String> peers = new ArrayList<>();

    public PeerList() {
    }

    public PeerList(ArrayList<String> peers) {
        this.peers = peers;
    }

    public ArrayList<String> getPeers() {
        return peers;
    }

    public void setPeers(ArrayList<String> peers) {
        this.peers = peers;
    }
}
