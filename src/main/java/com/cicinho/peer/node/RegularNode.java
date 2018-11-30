package com.cicinho.peer.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.crypto.ECKey;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.samples.BasicSample;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.SerializationUtils;

import com.cicinho.peer.transaction.PatientMedicalRecordTransaction;
import com.cicinho.peer.wallet.NodeWallet;
import com.google.gson.Gson;

public class RegularNode extends BasicSample {

	private Map<String, NodeWallet> nodeWalletsMap;

	public RegularNode(String logger) {
		// peers need different loggers
		super(logger);
		nodeWalletsMap = new HashMap<String, NodeWallet>();
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

		// PrivateKey
		//String privateKeySender = "3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c";
		// PublicKey
		String receiverPublicAddress = "5db10750e8caff27f906b41c71b3471057dd2004";

		// PrivateKey
		// String privateKeySender =
		// "6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec";
		// PublicKey
		// String receiverPublicAddress = "31e2e1ed11951c7091dfba62cd4b7145e947219c;

		// PrivateKey
		String privateKeySender =
		 "fee3b6045d75237490f1ba055bf6d034b2a83c71c78fb526b3183b5c68944f1d";
		// PublicKey
		//String receiverPublicAddress = "ee0250c19ad59305b2bdb61f34b45b72fe37154f";

		NodeWallet nodeWallet = new NodeWallet(privateKeySender);

		nodeWalletsMap.put(nodeWallet.getPublicKey(), nodeWallet);

		getAllTransactionsByWallet(nodeWallet);

		new Thread(() -> {
			@SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);
			int option, nonce;
			ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
			nonce = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue();
			do {
				System.out.println("MENU");
				System.out.println("Digite 1 para visualizar as transações enviadas pelo nó");
				System.out.println("Digite 2 para visualizar as transações recebidas pelo nó");
				System.out.println("Digite 3 para gerar uma PatientMedicalRecordTransaction");
				System.out.println("Digite 4 para gerar uma PatientMedicalRecordTransaction a cada 7 segundos");

				option = scanner.nextInt();

				switch (option) {				
				case 1:
					printSentTransactionByNode(nodeWallet);
					break;
				case 2:
					printReceivedTransactionByNode(nodeWallet);
					break;
				case 3:
					try {
						PatientMedicalRecordTransaction pmrt = new PatientMedicalRecordTransaction(
								nodeWallet.getPublicKey(), receiverPublicAddress, "/bloodTest", "POST", "RDA", 1580522400000L,
								"");
						sendOnePatientMedicalRecordTransaction(nonce, nodeWallet, new Gson().toJson(pmrt), receiverPublicAddress);
						++nonce;
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case 4:
					try {
						PatientMedicalRecordTransaction pmrt = new PatientMedicalRecordTransaction(
								nodeWallet.getPublicKey(), receiverPublicAddress, "/bloodTest", "POST", "RDA", 1580522400000L,
								"");
						while (true) {
							sendOnePatientMedicalRecordTransaction(nonce, nodeWallet, new Gson().toJson(pmrt), receiverPublicAddress);
							++nonce;
							Thread.sleep(3000);
						}
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
			Transaction transaction = receipt.getTransaction();
			String transactionSenderAddress = ByteUtil.toHexString(transaction.getSender());
			String transactionReceiverAddress = ByteUtil.toHexString(transaction.getReceiveAddress());
			if (nodeWalletsMap.containsKey(transactionSenderAddress)) {
				nodeWalletsMap.get(transactionSenderAddress).getSentTransactions().add(transaction);
			}
			if (nodeWalletsMap.containsKey(transactionReceiverAddress)) {
				nodeWalletsMap.get(transactionReceiverAddress).getReceivedTransactions().add(transaction);
			}
		}
	}

	/**
	 * Generate one simple value transfer transaction each 7 seconds. Thus blocks
	 * will include one, several and none transactions
	 */
	private void generateTransactions(NodeWallet nodeWallet, String receiverPublicAddress) throws Exception {
		logger.info("Start generating transactions...");

		// the sender which some coins from the genesis
		ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
		byte[] receiverAddr = Hex.decode(receiverPublicAddress);

		for (int i = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
			{
				Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
						ByteUtil.longToBytesNoLeadZeroes(0L), ByteUtil.longToBytesNoLeadZeroes(0xfffff), receiverAddr,
						new byte[] { 0 }, new byte[0], ethereum.getChainIdForNextBlock());
				tx.sign(senderKey);
				logger.info("<== Submitting tx: " + tx);
				ethereum.submitTransaction(tx);
			}
			Thread.sleep(7000);
		}
	}

	private void generateOneTransaction(int nonce, NodeWallet nodeWallet, String receiverPublicAddress)
			throws Exception {
		logger.info("Start generating a transaction...");

		// the sender which some coins from the genesis
		ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
		byte[] receiverAddr = Hex.decode(receiverPublicAddress);

		Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(nonce), ByteUtil.longToBytesNoLeadZeroes(0L),
				ByteUtil.longToBytesNoLeadZeroes(0xfffff), receiverAddr, new byte[] { 0 }, new byte[0],
				ethereum.getChainIdForNextBlock());
		tx.sign(senderKey);
		logger.info("<== Submitting tx: " + tx);
		ethereum.submitTransaction(tx);
	}

	private void sendOnePatientMedicalRecordTransaction(int nonce, NodeWallet nodeWallet,
			String pmrt, String receiverPublicAddress) throws Exception {
		logger.info("Start generating a transaction...");

		// the sender from the genesis
		ECKey senderKey = ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey()));
		byte[] receiverAddr = Hex.decode(receiverPublicAddress);

		Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(nonce), ByteUtil.longToBytesNoLeadZeroes(0L),
				ByteUtil.longToBytesNoLeadZeroes(0xfffff), receiverAddr, new byte[] { 0 },
				pmrt.getBytes(), ethereum.getChainIdForNextBlock());
		tx.sign(senderKey);
		logger.info("<== Submitting tx: " + tx);
		ethereum.submitTransaction(tx);
	}

	private void getBalances(NodeWallet nodeWallet) {
		System.out.println("BALANCES");
		System.out.println("Balance SENDER: " + ethereum.getRepository()
				.getBalance(ECKey.fromPrivate(Hex.decode(nodeWallet.getPrivateKey())).getAddress()));
	}

	private void printSentTransactionByNode(NodeWallet nodeWallet) {
		System.out.println("\nSent Transactions by this Node:");
		printTransactions(nodeWallet.getSentTransactions());
		System.out.println("\n\n\n");
	}

	private void printReceivedTransactionByNode(NodeWallet nodeWallet) {
		System.out.println("\nReceive Transactions by this Node:");
		printTransactions(nodeWallet.getReceivedTransactions());
		System.out.println("\n\n\n");
	}

	private void printTransactions(List<Transaction> transactions) {
		for (Transaction t : transactions) {
			System.out.println("\n" + t.toString());
			if (t.getData() != null) {
				try {		
					System.out.println(new String(t.getData(), "UTF-8"));
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		}
	}	

	private NodeWallet getAllTransactionsByWallet(NodeWallet nodeWallet) {
		String nodeWalletAddress = nodeWallet.getPublicKey();
		// Block 0 is the genesis block
		for (int i = 1; i < ethereum.getBlockchain().getBestBlock().getNumber(); i++) {
			for (Transaction t : ethereum.getBlockchain().getBlockByNumber(i).getTransactionsList()) {
				if (nodeWalletAddress.equals(ByteUtil.toHexString(t.getSender()))) {
					nodeWallet.getSentTransactions().add(t);
				} else if (nodeWalletAddress.equals(ByteUtil.toHexString(t.getReceiveAddress()))) {
					nodeWallet.getReceivedTransactions().add(t);
				}
			}
		}
		return nodeWallet;
	}
}
