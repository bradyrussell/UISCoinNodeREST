package com.bradyrussell.uiscoin;

import com.bradyrussell.uiscoin.address.UISCoinAddress;
import com.bradyrussell.uiscoin.address.UISCoinKeypair;
import com.bradyrussell.uiscoin.blockchain.BlockChain;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchBlockException;
import com.bradyrussell.uiscoin.blockchain.exception.NoSuchTransactionException;
import com.bradyrussell.uiscoin.transaction.TransactionBuilder;
import com.bradyrussell.uiscoin.transaction.TransactionInputBuilder;
import com.bradyrussell.uiscoin.transaction.TransactionOutput;

import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UISCoinHelper {
    public static long getBalanceForKeypairs(List<UISCoinKeypair> keypairs) {
        long total = 0;

        for (UISCoinKeypair keypair : keypairs) {
            try {
                ArrayList<byte[]> outputsToAddress = BlockChain.get().matchUTXOForP2PKHAddress(UISCoinAddress.decodeAddress(UISCoinAddress.fromPublicKey((ECPublicKey) keypair.Keys.getPublic())).HashData);
                for (byte[] toAddress : outputsToAddress) {
                    byte[] TsxnHash = new byte[64];
                    byte[] IndexBytes = new byte[4];

                    System.arraycopy(toAddress, 0, TsxnHash, 0, 64);
                    System.arraycopy(toAddress, 64, IndexBytes, 0, 4);

                    long amount;
                    try {
                        amount = BlockChain.get().getUnspentTransactionOutput(TsxnHash, BytesUtil.ByteArrayToNumber32(IndexBytes)).Amount;
                    } catch (NoSuchTransactionException e) {
                        e.printStackTrace();
                        continue;
                    }
                    total += amount;
                }
            } catch (Exception e) {
                e.printStackTrace();
                //continue without failing
            }
        }
        return total;
    }
}
