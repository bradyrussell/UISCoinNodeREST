package com.bradyrussell.uiscoin.controller;

import com.bradyrussell.uiscoin.UISCoinContext;
import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.data.BooleanSuccessResult;
import com.bradyrussell.uiscoin.data.NodeStatus;
import com.bradyrussell.uiscoin.data.PeerList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
public class NodeRestController {
    @GetMapping(value = {"/", "/status"})
    public NodeStatus status() {
        boolean running = UISCoinContext.getNode() != null;
        int peers = running ? UISCoinContext.getNode().getPeers().size() : 0;
        return new NodeStatus(running, peers > 0, peers, running ? BlockChain.get().BlockHeight : -1);
    }

    @GetMapping(value = {"/start"})
    public BooleanSuccessResult start() {
        return new BooleanSuccessResult(UISCoinContext.start());
    }

    @GetMapping(value = {"/stop"})
    public BooleanSuccessResult stop() {
        return new BooleanSuccessResult(UISCoinContext.stop());
    }

    @GetMapping(value = {"/peers"})
    public PeerList peers() {
        return new PeerList(UISCoinContext.getNode().getPeers().stream().map((InetAddress::getHostAddress)).collect(Collectors.toCollection(ArrayList::new)));
    }

    @PostMapping(value = {"/peers"})
    public PeerList addPeers(@RequestParam(value = "peers", defaultValue = "") String peers) {
        String[] peerStrings = peers.split("\n");
        for (String peerString : peerStrings) {
            if (peerString.isBlank()) continue;
            try {
                UISCoinContext.getNode().ConnectToPeer(InetAddress.getByName(peerString));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return new PeerList(UISCoinContext.getNode().getPeers().stream().map((InetAddress::getHostAddress)).collect(Collectors.toCollection(ArrayList::new))); // will not contain new peers as connection is async
    }

    @GetMapping(value = {"/retrypeers"})
    public BooleanSuccessResult reconnect() {
        if (UISCoinContext.getNode() != null) {
            UISCoinContext.getNode().RetryPeers();
            return new BooleanSuccessResult(true);
        }
        return new BooleanSuccessResult(false);
    }
}
