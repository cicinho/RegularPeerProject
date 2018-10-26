package com.cicinho.peer.wallet;

import java.util.ArrayList;
import java.util.List;

import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.springframework.stereotype.Component;

@Component
public class NodeWallet {

	List<Transaction> sentTransactions;
	
	List<Transaction> receivedTransactions;
	
	List<TransactionReceipt> sentTransactionReceipts;
	
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
