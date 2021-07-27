package com.bradyrussell.uiscoin.data;

import com.bradyrussell.uiscoin.transaction.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemPoolMap {
    private HashMap<String, Transaction> mempool = new HashMap<>();

    public HashMap<String, Transaction> getMempool() {
        return mempool;
    }

    public void setMempool(HashMap<String, Transaction> mempool) {
        this.mempool = mempool;
    }

    public MemPoolMap(HashMap<String, Transaction> mempool) {
        this.mempool = mempool;
    }
}
