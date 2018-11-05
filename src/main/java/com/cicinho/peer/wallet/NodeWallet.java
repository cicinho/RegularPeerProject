package com.cicinho.peer.wallet;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

public class NodeWallet {

	private String privateKey;
	
	private List<Transaction> sentTransactions;
	
	private List<Transaction> receivedTransactions;
	
	public NodeWallet(String privateKey) {
		this.sentTransactions = new ArrayList<>();
		this.receivedTransactions = new ArrayList<>();
		this.privateKey = privateKey;
	}		

	public String getPrivateKey() {
		return privateKey;
	}
	
	public String getPublicKey () {		
		return ByteUtil.toHexString(ECKey.fromPrivate(Hex.decode(getPrivateKey())).getAddress());
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public List<Transaction> getSentTransactions() {
		return sentTransactions;
	}

	public List<Transaction> getReceivedTransactions() {
		return receivedTransactions;
	}
}
