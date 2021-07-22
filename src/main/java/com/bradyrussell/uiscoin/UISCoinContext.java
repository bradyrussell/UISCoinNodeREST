package com.bradyrussell.uiscoin;

import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.blockchain.BlockChainStorageFile;
import com.bradyrussell.uiscoin.node.Node;

import java.nio.file.Path;

public class UISCoinContext {
    private static Node node = null;

    public static boolean start(){
        if(node != null) return false;
        BlockChain.Storage = new BlockChainStorageFile();
        BlockChain.get().open();
        node = new Node(1);
        node.Start();
        UISCoinUtil.syncBlockChain(node, Path.of("peers.txt"));
        return true;
    }

    public static boolean stop(){
        if(node == null) return false;
        node.Stop();
        node = null;
        BlockChain.get().close();
        BlockChain.Storage = null;
        return true;
    }

    public static Node getNode() {
        return node;
    }
}
