package com.bradyrussell.uiscoin;

import com.bradyrussell.uiscoin.address.UISCoinKeypair;
import com.bradyrussell.uiscoin.address.Wallet;
import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.blockchain.exception.InvalidBlockException;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchBlockException;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchTransactionException;
import com.bradyrussell.uiscoin.data.PeerList;
import com.bradyrussell.uiscoin.node.Node;
import com.bradyrussell.uiscoin.script.ScriptMatcher;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UISCoinUtil {
    public static void syncBlockChain(Node node, Path peerlist) {
        System.out.println("Starting node with " + setupPeers(node, peerlist) + " peers.");

        try { // need time to allow peer connections
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Syncing blockheight with peers. " + node.getPeers().size());
        node.RequestBlockHeightFromPeers();

        if (node.getPeers().size() > 0 || BlockChain.get().BlockHeight < 0) { // we cant skip this if we have never had the blockchain
            while (node.HighestSeenBlockHeight == -1) {
                try {
                    System.out.println("Waiting for blockheight sync...");
                    Thread.sleep(500);
                    node.RequestBlockHeightFromPeers();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("BlockHeight synced.");

            while (BlockChain.get().BlockHeight < node.HighestSeenBlockHeight) {
                try {
                    System.out.println("Waiting for blockchain sync... Have: " + BlockChain.get().BlockHeight + " Seen: " + node.HighestSeenBlockHeight);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("BlockChain synced.");
        }

        boolean verify = false;
        try {
            verify = BlockChain.Verify(0);
            BlockChain.BuildUTXOSet(0);
        } catch (NoSuchBlockException | InvalidBlockException | NoSuchTransactionException e) {
            e.printStackTrace();
        }


        System.out.println("Full BlockChain verification: " + verify);
        //if (!verify) System.exit(100); // todo disconnect that node and retry
    }

    public static void savePeers(Node node, Path peerlist) {
        StringBuilder sb = new StringBuilder();
        for (InetAddress peerAddr : node.getPeers()) {
            sb.append(peerAddr.getHostAddress());
            sb.append("\n");
        }

        try {
            Files.writeString(peerlist, sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int setupPeers(Node node, Path peerlist) {
        List<String> connected = new ArrayList<>();

        if (Files.exists(peerlist)) {
            System.out.println("Loading peerlist...");
            try {
                List<String> peers = Files.readAllLines(peerlist);

                for (String peer : peers) {
                    if (!peer.isEmpty() && !connected.contains(peer)) {
                        System.out.println("Connecting to peer " + peer);
                        node.ConnectToPeer(InetAddress.getByName(peer));
                        connected.add(peer);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //if (IgnorePeerRepo) return connected.size();

        System.out.println("Getting peerlist from repository...");
        String request = null;
        try {
            request = HTTP.Request("https://raw.githubusercontent.com/bradyrussell/UISCoinNodes/master/nodes.txt", "GET", null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES,true);
        PeerList peerList = null;
        try {
            peerList = objectMapper.readValue(request, PeerList.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

/*        JsonObject json = new Gson().fromJson(request, JsonObject.class);
        JsonArray peers = json.get("peers").getAsJsonArray();*/

        System.out.println("Found " + peerList.getPeers().size() + " peers.");

        peerList.getPeers().forEach((peerString -> {
            try {
                if (!connected.contains(peerString)) {
                    System.out.println("Connecting to repository peer " + peerString);
                    node.ConnectToPeer(InetAddress.getByName(peerString));
                    connected.add(peerString);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }));
        return connected.size();
    }

    public static String getScriptDescription(byte[] script) {
        ScriptMatcher matcherP2PK = ScriptMatcher.getMatcherP2PK();
        if(matcherP2PK.match(script)) {
            return "Pay to Public Key: "+BytesUtil.Base64Encode(matcherP2PK.getPushData(0)) + (matcherP2PK.getPushCount() > 1 ? " Memo: "+new String(matcherP2PK.getPushData(1)) : "");
        }
        ScriptMatcher matcherP2PKH = ScriptMatcher.getMatcherP2PKH();
        if(matcherP2PKH.match(script)) {
            return "Pay to Public Key Hash: "+BytesUtil.Base64Encode(matcherP2PKH.getPushData(0)) + (matcherP2PKH.getPushCount() > 1 ? " Memo: "+new String(matcherP2PKH.getPushData(1)) : "");
        }
        ScriptMatcher matcherP2Password = ScriptMatcher.getMatcherP2Password();
        if(matcherP2Password.match(script)) {
            return "Pay to Password: "+BytesUtil.Base64Encode(matcherP2Password.getPushData(0)) + (matcherP2Password.getPushCount() > 1 ? " Memo: "+new String(matcherP2Password.getPushData(1)) : "");
        }
        ScriptMatcher matcherP2SH = ScriptMatcher.getMatcherP2SH();
        if(matcherP2SH.match(script)) {
            return "Pay to Script Hash: "+BytesUtil.Base64Encode(matcherP2SH.getPushData(0)) + (matcherP2SH.getPushCount() > 1 ? " Memo: "+new String(matcherP2SH.getPushData(1)) : "");
        }

        return null;
    }
}
