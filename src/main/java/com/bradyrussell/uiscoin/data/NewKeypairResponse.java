package com.bradyrussell.uiscoin.data;

import com.bradyrussell.uiscoin.address.UISCoinAddress;
import com.bradyrussell.uiscoin.address.UISCoinKeypair;

import java.security.interfaces.ECPublicKey;
import java.util.Base64;

public class NewKeypairResponse {
    private UISCoinKeypair keypair;

    public byte[] getPublicKey() {
        return keypair.Keys.getPublic().getEncoded();
    }

    public String getAddress() {
        return Base64.getUrlEncoder().encodeToString(UISCoinAddress.fromPublicKey((ECPublicKey) keypair.Keys.getPublic()));
    }

    public byte[] getPrivateKey() {
        return keypair.Keys.getPrivate().getEncoded();
    }

    public byte[] getSeed() {
        return keypair.Seed;
    }

    public NewKeypairResponse(UISCoinKeypair keypair) {
        this.keypair = keypair;
    }
}
