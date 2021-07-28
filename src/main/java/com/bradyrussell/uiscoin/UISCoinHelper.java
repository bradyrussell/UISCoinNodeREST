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
import java.util.*;

public class UISCoinHelper {
    public static long getBalanceForKeypairs(List<UISCoinKeypair> keypairs) {
        long total = 0;

        for (UISCoinKeypair keypair : keypairs) {
            try {
                System.out.println("Finding utxo for "+ Base64.getUrlEncoder().encodeToString(UISCoinAddress.fromPublicKey((ECPublicKey) keypair.Keys.getPublic())));
                ArrayList<byte[]> outputsToAddress = BlockChain.get().matchUTXOForP2PKHAddress(UISCoinAddress.decodeAddress(UISCoinAddress.fromPublicKey((ECPublicKey) keypair.Keys.getPublic())).HashData);
                for (byte[] toAddress : outputsToAddress) {
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
                    System.out.println("Found "+amount);
                    total += amount;
                }
            } catch (Exception e) {
                e.printStackTrace();
                //continue without failing
            }
        }
        System.out.println("Found "+total+" or "+Conversions.SatoshisToCoins(total)+" UISCoin");
        return total;
    }
}
