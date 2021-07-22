package com.bradyrussell.uiscoin.data;

import com.bradyrussell.uiscoin.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class MemPoolList {
    private List<Transaction> mempool = new ArrayList<>();

    public MemPoolList(List<Transaction> mempool) {
        this.mempool = mempool;
    }

    public List<Transaction> getMempool() {
        return mempool;
    }
}
