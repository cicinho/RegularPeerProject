package com.cicinho.peer.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.db.ByteArrayWrapper;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.samples.BasicSample;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.SerializationUtils;

import com.cicinho.peer.transaction.PatientMedicalRecordTransaction;
import com.cicinho.peer.wallet.NodeWallet;

public class RegularNode extends BasicSample {

	// PrivateKey
	String privateKeySender = "3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c";
	// PublicKey
	// "5db10750e8caff27f906b41c71b3471057dd2004"

	// PrivateKey
	// "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"
	// PublicKey
	// "31e2e1ed11951c7091dfba62cd4b7145e947219c

	// PrivateKey
	// "fee3b6045d75237490f1ba055bf6d034b2a83c71c78fb526b3183b5c68944f1d"
	// PublicKey
	private String receiverPublicAddress = "ee0250c19ad59305b2bdb61f34b45b72fe37154f";

	private NodeWallet nodeWallet;

	private Map<ByteArrayWrapper, TransactionReceipt> txWaiters = Collections
			.synchronizedMap(new HashMap<ByteArrayWrapper, TransactionReceipt>());

	public RegularNode(String logger) {
		// peers need different loggers
		super(logger);

		nodeWallet = new NodeWallet(privateKeySender);
	}

	@Override
	public void onSyncDone() {
		ethereum.addListener(new EthereumListenerAdapter() {
			// when block arrives look for our included transactions
			@Override
			public void onBlock(Block block, List<TransactionReceipt> receipts) {
				RegularNode.this.onBlock(block, receipts);
			}
		});
		
		getAllTransactionsByWallet(nodeWallet);

		new Thread(() -> {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			int option, nonce;
			ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
			nonce = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue();
			do {
				System.out.println("MENU");
				System.out.println("Digite 1 para gerar um transação");
				System.out.println("Digite 2 para gerar uma transação a cada 7 segundos");
				System.out.println("Digite 3 para visualizar o balanço das contas");
				System.out.println("Digite 4 para visualizar as transações enviadas pelo nó");
				System.out.println("Digite 5 para visualizar as transações recebidas pelo nó");
				System.out.println("Digite 6 para gerar uma PatientMedicalRecordTransaction");

				option = scanner.nextInt();

				switch (option) {
				case 1:
					try {
						generateOneTransaction(nonce);
						++nonce;
					} catch (Exception e) {
						logger.error("Error generating tx: ", e);
					}
					break;
				case 2:
					try {
						generateTransactions();
					} catch (Exception e) {
						logger.error("Error generating tx: ", e);
					}
					break;
				case 3:
					getBalances();
					break;
				case 4:
					printSentTransactionByNode(nodeWallet);
					break;
				case 5:
					printReceivedTransactionByNode(nodeWallet);
					break;
				case 6:
					try {
						generateOnePatientMedicalRecordTransaction(nonce);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
				}
			} while (option != 0);
		}).start();
	}

	private void onBlock(Block block, List<TransactionReceipt> receipts) {
		for (TransactionReceipt receipt : receipts) {
			ByteArrayWrapper txHashW = new ByteArrayWrapper(receipt.getTransaction().getHash());
			if (txWaiters.containsKey(txHashW)) {
				txWaiters.put(txHashW, receipt);
				synchronized (this) {
					notifyAll();
				}
			} else if (nodeWallet.getPublicKey().equals(ByteUtil.toHexString(receipt.getTransaction().getReceiveAddress()))) {
				nodeWallet.getReceivedTransactions().add(receipt.getTransaction());
			}
		}
	}

	/**
	 * Generate one simple value transfer transaction each 7 seconds. Thus blocks
	 * will include one, several and none transactions
	 */
	private void generateTransactions() throws Exception {
		logger.info("Start generating transactions...");

		// the sender which some coins from the genesis
		ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
		byte[] receiverAddr = Hex.decode(receiverPublicAddress);

		for (int i = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
			{
				Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
						ByteUtil.longToBytesNoLeadZeroes(0L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
						receiverAddr, new byte[] { 77 }, new byte[0], ethereum.getChainIdForNextBlock());
				tx.sign(senderKey);
				logger.info("<== Submitting tx: " + tx);
				ethereum.submitTransaction(tx);
			}
			Thread.sleep(7000);
		}
	}

	private void generateOneTransaction(int nonce) throws Exception {
		logger.info("Start generating a transaction...");

		// the sender which some coins from the genesis
		ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
		byte[] receiverAddr = Hex.decode(receiverPublicAddress);

		Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(nonce),
				ByteUtil.longToBytesNoLeadZeroes(0L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
				receiverAddr, new byte[] { 77 }, new byte[0], ethereum.getChainIdForNextBlock());
		tx.sign(senderKey);
		logger.info("<== Submitting tx: " + tx);
		ethereum.submitTransaction(tx);

		new Thread(() -> {
			try {
				TransactionReceipt tr = waitForTx(tx.getHash());
				nodeWallet.getSentTransactions().add(tr.getTransaction());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	private void generateOnePatientMedicalRecordTransaction(int nonce) throws Exception {
		logger.info("Start generating a transaction...");

		// the sender from the genesis
		ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
		
		byte[] receiverAddr = Hex.decode(receiverPublicAddress);

		PatientMedicalRecordTransaction pmrt = new PatientMedicalRecordTransaction(nodeWallet.getPublicKey(), receiverPublicAddress, "/bloodTest", "POST", "RAD", 86400, "");
		Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(nonce),
				ByteUtil.longToBytesNoLeadZeroes(0L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
				receiverAddr, new byte[] { 0 }, SerializationUtils.serialize(pmrt), ethereum.getChainIdForNextBlock());
	
		tx.sign(senderKey);
		logger.info("<== Submitting tx: " + tx);
		ethereum.submitTransaction(tx);

		new Thread(() -> {
			try {
				TransactionReceipt tr = waitForTx(tx.getHash());
				nodeWallet.getSentTransactions().add(tr.getTransaction());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	protected TransactionReceipt waitForTx(byte[] txHash) throws InterruptedException {
		ByteArrayWrapper txHashW = new ByteArrayWrapper(txHash);
		txWaiters.put(txHashW, null);
		long startBlock = ethereum.getBlockchain().getBestBlock().getNumber();
		while (true) {
			TransactionReceipt receipt = txWaiters.get(txHashW);
			if (receipt != null) {
				// TODO armazenar transações enviadas pela wallet
				return receipt;
			} else {
				long curBlock = ethereum.getBlockchain().getBestBlock().getNumber();
				if (curBlock > startBlock + 16) {
					throw new RuntimeException("The transaction was not included during last 16 blocks: "
							+ txHashW.toString().substring(0, 8));
				} else {
					logger.info("Waiting for block with transaction 0x" + txHashW.toString().substring(0, 8)
							+ " included (" + (curBlock - startBlock) + " blocks received so far) ...");
				}
			}
			synchronized (this) {
				wait(20000);
			}
		}
	}

	private void getBalances() {
		System.out.println("BALANCES");
		System.out.println("Balance MINER: "
				+ ethereum.getRepository().getBalance(Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c")));
		System.out.println("Balance SENDER: " + ethereum.getRepository()
				.getBalance(ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey())).getAddress()));
		System.out.println("Balance RECEIVER: " + ethereum.getRepository().getBalance(Hex.decode(receiverPublicAddress))
				+ "\n\n\n");
	}

	private void printSentTransactionByNode(NodeWallet nodeWallet) {
		System.out.println("\nSent Transactions by this Node:");
		for (Transaction t : nodeWallet.getSentTransactions()) {
			System.out.println(t.toString());
			if (t.getData() != null) 
				System.out.println(SerializationUtils.deserialize(t.getData()) + "\n");
		}
		System.out.println("\n\n\n");
	}
	
	private void printReceivedTransactionByNode(NodeWallet nodeWallet) {
		System.out.println("\nReceive Transactions by this Node:");
		for (Transaction t : nodeWallet.getReceivedTransactions()) {
			System.out.println(t.toString());
		}
		System.out.println("\n\n\n");
	}
	
	private NodeWallet getAllTransactionsByWallet(NodeWallet nodeWallet) {
		String nodeWalletAddress = nodeWallet.getPublicKey();
		//Block 0 is the genesis block
		for(int i = 1; i < ethereum.getBlockchain().getBestBlock().getNumber(); i++) {
			for (Transaction t: ethereum.getBlockchain().getBlockByNumber(i).getTransactionsList()) {
				if(nodeWalletAddress.equals(ByteUtil.toHexString(t.getSender()))) {
					nodeWallet.getSentTransactions().add(t);
				} else if(nodeWalletAddress.equals(ByteUtil.toHexString(t.getReceiveAddress()))) {
					nodeWallet.getReceivedTransactions().add(t);
				}
			}
		}		
		return nodeWallet;
	}
}
