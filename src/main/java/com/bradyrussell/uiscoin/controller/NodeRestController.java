package com.bradyrussell.uiscoin.controller;

import com.bradyrussell.uiscoin.*;
import com.bradyrussell.uiscoin.address.UISCoinAddress;
import com.bradyrussell.uiscoin.address.UISCoinKeypair;
import com.bradyrussell.uiscoin.block.Block;
import com.bradyrussell.uiscoin.block.BlockHeader;
import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchBlockException;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchTransactionException;
import com.bradyrussell.uiscoin.data.*;
import com.bradyrussell.uiscoin.transaction.Transaction;
import com.bradyrussell.uiscoin.transaction.TransactionBuilder;
import com.bradyrussell.uiscoin.transaction.TransactionOutputBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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
    public MemPoolMap mempool() {
        if (UISCoinContext.getNode() != null) {
            HashMap<String, Transaction> mempool = new HashMap<>();
            for (Transaction transaction : BlockChain.get().getMempool()) {
                mempool.put(Base64.getUrlEncoder().encodeToString(transaction.getHash()), transaction);
            }

            return new MemPoolMap(mempool);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }

    @GetMapping(value = {"/script"})
    public ScriptTypeMessage script(@RequestParam(value = "script") String script) {
        if (UISCoinContext.getNode() != null) {
            String scriptDescription = UISCoinUtil.getScriptDescription(Base64.getUrlDecoder().decode(script));
            return new ScriptTypeMessage(scriptDescription == null ? "Unknown script!":scriptDescription);
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }

    @GetMapping(value = {"/unspent"})
    public UTXOs unspent(@RequestParam(value = "address") String address) {
        if (UISCoinContext.getNode() != null) {
            if(!UISCoinAddress.verifyAddressChecksum(BytesUtil.Base64Decode(address))) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid P2PKH address!");
            byte[] publicKeyHash = UISCoinAddress.decodeAddress(BytesUtil.Base64Decode(address)).HashData;
            return new UTXOs(BlockChain.get().matchUTXOForP2PKHAddress(publicKeyHash));
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }

    @GetMapping(value = {"/keypair"})
    public NewKeypairResponse keypair() {
        return new NewKeypairResponse(UISCoinKeypair.Create());
    }

    @PostMapping(value = {"/keypair"})
    public NewKeypairResponse keypairPost(@RequestParam(value = "keypair") String keypair, @RequestParam(value = "password", required = false) String password) {
        try {
            UISCoinKeypair uisCoinKeypair = new UISCoinKeypair();
            byte[] data = Base64.getUrlDecoder().decode(keypair);
            if(password == null) {
                uisCoinKeypair.setBinaryData(data);
            } else {
                uisCoinKeypair.setBinaryData(Encryption.Decrypt(data, Hash.getSHA512Bytes(password)));
            }
            return new NewKeypairResponse(uisCoinKeypair);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.toString());
        }
    }

    @GetMapping(value = {"/difficulty"})
    public DifficultyResponse difficulty() {
        try {
            BlockHeader lastBlockHeader = BlockChain.get().getBlockHeader(BlockChain.get().HighestBlockHash);
            long timestamp = Instant.now().getEpochSecond();
            return new DifficultyResponse(BlockHeader.CalculateDifficultyTarget(timestamp - lastBlockHeader.Time, lastBlockHeader.DifficultyTarget));
        } catch (NoSuchBlockException e) {
            e.printStackTrace();
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }

    @PostMapping(value = {"/balance"})
    public BalanceResult balance(@RequestBody BalanceData balanceData) {
        ArrayList<UISCoinKeypair> keypairs = new ArrayList<>();

        for (String keypair : balanceData.getKeypairs()) {
            try {
                UISCoinKeypair uisCoinKeypair = new UISCoinKeypair();
                uisCoinKeypair.setBinaryData(Base64.getUrlDecoder().decode(keypair));
                keypairs.add(uisCoinKeypair);
            } catch (Exception e) {
                e.printStackTrace();
                // ignore
            }
        }

        return new BalanceResult(UISCoinHelper.getBalanceForKeypairs(keypairs));
    }

    @PostMapping(value = {"/send"})
    public TransactionHashResult send(@RequestBody SendData sendData) {
        if(!UISCoinAddress.verifyAddressChecksum(Base64.getUrlDecoder().decode(sendData.getRecipient()))) throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Invalid address!");

        ArrayList<UISCoinKeypair> keypairs = new ArrayList<>();

        for (String keypair : sendData.getKeypairs()) {
            try {
                UISCoinKeypair uisCoinKeypair = new UISCoinKeypair();
                uisCoinKeypair.setBinaryData(Base64.getUrlDecoder().decode(keypair));
                keypairs.add(uisCoinKeypair);
            } catch (Exception e){
                e.printStackTrace();
                // ignore
            }
        }

        if (UISCoinContext.getNode() != null) {
            TransactionOutputBuilder outputBuilder = new TransactionOutputBuilder()
                    .setPayToPublicKeyHash(UISCoinAddress.decodeAddress(Base64.getUrlDecoder().decode(sendData.getRecipient())).HashData)
                    .setAmount(sendData.getAmount());

            if(sendData.getMemo() != null && !sendData.getMemo().isBlank()) {
                outputBuilder.setMemo(sendData.getMemo());
            }

            try {
                Transaction transaction = new TransactionBuilder()
                        .setVersion(MagicBytes.ProtocolVersion.Value)
                        .addOutput(outputBuilder.get())
                        .addInputsFromMultipleKeypairsP2pkh(keypairs, sendData.getAmount()+sendData.getFee())
                        .addChangeOutputToPublicKeyHash(UISCoinAddress.decodeAddress(UISCoinAddress.fromPublicKey((ECPublicKey) keypairs.get(0).Keys.getPublic())).HashData, sendData.getFee())
                        .get();

                if(transaction.Verify()) {
                    UISCoinContext.getNode().BroadcastTransactionToPeers(transaction);
                    return new TransactionHashResult(Base64.getUrlEncoder().encodeToString(transaction.getHash()));
                } else {
                    throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Transaction verification failed!");
                }

            } catch (NoSuchTransactionException | NoSuchBlockException e) {
                e.printStackTrace();
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Not initialized!");
    }
}
