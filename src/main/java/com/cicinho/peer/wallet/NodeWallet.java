package com.cicinho.peer.wallet;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;

public class NodeWallet {

	private List<Transaction> sentTransactions;
	
	private List<Transaction> receivedTransactions;
	
	private List<TransactionReceipt> sentTransactionReceipts;
	
	public NodeWallet() {
		this.sentTransactions = new ArrayList<>();
		this.receivedTransactions = new ArrayList<>();
		this.sentTransactionReceipts = new ArrayList<>();
	}

	public List<Transaction> getSentTransactions() {
		return sentTransactions;
	}

	public List<Transaction> getReceivedTransactions() {
		return receivedTransactions;
	}

	public List<TransactionReceipt> getSentTransactionReceipts() {
		return sentTransactionReceipts;
	}	
	
}
