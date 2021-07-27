package com.bradyrussell.uiscoin.data;

import java.util.List;

public class SendData {
    private List<String> keypairs;
    private long amount;
    private long fee;
    private String memo;
    private String recipient;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public List<String> getKeypairs() {
        return keypairs;
    }

    public void setKeypairs(List<String> keypairs) {
        this.keypairs = keypairs;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
