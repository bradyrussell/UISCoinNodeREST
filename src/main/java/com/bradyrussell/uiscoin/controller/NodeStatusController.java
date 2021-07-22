package com.bradyrussell.uiscoin.controller;

import com.bradyrussell.uiscoin.UISCoinContext;
import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.data.BooleanSuccessResult;
import com.bradyrussell.uiscoin.data.NodeStatus;
import com.bradyrussell.uiscoin.data.PeerList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@RestController
public class NodeStatusController {
    @GetMapping(value={"/", "/status"})
    public NodeStatus status() {
        boolean running = UISCoinContext.getNode() != null;
        return new NodeStatus(running,running && UISCoinContext.getNode().getPeers().size() > 0, running ? BlockChain.get().BlockHeight : -1);
    }

    @GetMapping(value={"/start"})
    public BooleanSuccessResult start() {
        return new BooleanSuccessResult(UISCoinContext.start());
    }

    @GetMapping(value={"/stop"})
    public BooleanSuccessResult stop() {
        return new BooleanSuccessResult(UISCoinContext.stop());
    }

    @GetMapping(value={"/peers"})
    public PeerList peers() {
        return new PeerList(UISCoinContext.getNode().getPeers().stream().map((InetAddress::getHostAddress)).collect(Collectors.toCollection(ArrayList::new)));
    }
}
