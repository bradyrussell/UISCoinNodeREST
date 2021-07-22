package com.bradyrussell.uiscoin.controller;

import com.bradyrussell.uiscoin.UISCoinContext;
import com.bradyrussell.uiscoin.block.Block;
import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchBlockException;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchTransactionException;
import com.bradyrussell.uiscoin.data.BooleanSuccessResult;
import com.bradyrussell.uiscoin.data.NodeStatus;
import com.bradyrussell.uiscoin.data.PeerList;
import com.bradyrussell.uiscoin.transaction.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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

    @GetMapping(value = {"/block"})
    public Block block(@RequestParam(value = "blockHash", required = false) String blockHash, @RequestParam(value = "blockHeight", required = false) Integer blockHeight) {
        if (UISCoinContext.getNode() != null) {
            if(blockHash != null) {
                try {
                    return BlockChain.get().getBlock(Base64.getUrlDecoder().decode(blockHash));
                } catch (NoSuchBlockException e) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
                }
            } else if(blockHeight != null){
                try {
                    return BlockChain.get().getBlockByHeight(blockHeight);
                } catch (NoSuchBlockException e) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
                }
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }

    @GetMapping(value = {"/transaction"})
    public Transaction transaction(@RequestParam(value = "transactionHash") String transactionHash) {
        if (UISCoinContext.getNode() != null) {
            try {
                return BlockChain.get().getTransaction(Base64.getUrlDecoder().decode(transactionHash));
            } catch (NoSuchTransactionException | NoSuchBlockException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }

    @GetMapping(value = {"/mempool"})
    public List<Transaction> mempool() {
        if (UISCoinContext.getNode() != null) {
            return BlockChain.get().getMempool();
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }
}
