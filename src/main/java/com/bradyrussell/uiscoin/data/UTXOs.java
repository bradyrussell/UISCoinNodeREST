package com.bradyrussell.uiscoin.data;

import com.bradyrussell.uiscoin.BytesUtil;
import com.bradyrussell.uiscoin.Conversions;
import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchTransactionException;

import java.util.ArrayList;
import java.util.List;

public class UTXOs {
    public static class UTXO {
        public byte[] hash;
        public int index;
        public long amount;

        public byte[] getHash() {
            return hash;
        }

        public int getIndex() {
            return index;
        }

        public long getAmount() {
            return amount;
        }

        public UTXO(byte[] hash, int index, long amount) {
            this.hash = hash;
            this.index = index;
            this.amount = amount;
        }
    }

    public final List<UTXO> utxos;
    public final long total;

    public List<UTXO> getUtxos() {
        return utxos;
    }

    public long getTotal() {
        return total;
    }

    public UTXOs(List<byte[]> utxoList) {
        utxos = new ArrayList<>();
        long runningTotal = 0;
        for (byte[] toAddress : utxoList) {
            byte[] TsxnHash = new byte[64];
            byte[] IndexBytes = new byte[4];

            System.arraycopy(toAddress, 0, TsxnHash, 0, 64);
            System.arraycopy(toAddress, 64, IndexBytes, 0, 4);

            long amount = 0;
            try {
                amount = BlockChain.get().getUnspentTransactionOutput(TsxnHash, BytesUtil.ByteArrayToNumber32(IndexBytes)).Amount;
            } catch (NoSuchTransactionException e) {
                e.printStackTrace();
            }
            //System.out.println("Found unspent output from transaction " + BytesUtil.Base64Encode(TsxnHash) + " index " + BytesUtil.ByteArrayToNumber32(IndexBytes) + " with " + Conversions.SatoshisToCoins(amount) + " UISCoins or " + amount + " satoshis.");
            utxos.add(new UTXO(TsxnHash, BytesUtil.ByteArrayToNumber32(IndexBytes), amount));
            runningTotal += amount;
        }
        total = runningTotal;
    }
}
