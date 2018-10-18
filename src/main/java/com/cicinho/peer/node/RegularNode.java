package com.cicinho.peer.node;

import java.util.Scanner;

import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.samples.BasicSample;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

public class RegularNode extends BasicSample {

	public RegularNode(String logger) {
		// peers need different loggers
		super(logger);
	}

	@Override
	public void onSyncDone() {
		/*
		 * new Thread(() -> { try { generateTransactions(); } catch (Exception e) {
		 * logger.error("Error generating tx: ", e); } }).start();
		 */
		Scanner scanner = new Scanner(System.in);
		int option;
		do {
			System.out.println("MENU");
			System.out.println("Digite 1 para gerar um transação");
			System.out.println("Digite 2 para gerar uma transação a cada 7 segundos");

			option = scanner.nextInt();

			switch (option) {
			case 1:
				try {
					generateOneTransaction();
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
			default:
				break;
			}
		} while (option != 0);
	}

	/**
	 * Generate one simple value transfer transaction each 7 seconds. Thus blocks
	 * will include one, several and none transactions
	 */
	private void generateTransactions() throws Exception {
		logger.info("Start generating transactions...");

		// the sender which some coins from the genesis
		ECKey senderKey = ECKey
				.fromPrivate(Hex.decode("3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c"));
		byte[] receiverAddr = Hex.decode("5db10750e8caff27f906b41c71b3471057dd2004");

		for (int i = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue(), j = 0; j < 20000; i++, j++) {
			{
				Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
						ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
						receiverAddr, new byte[] { 77 }, new byte[0], ethereum.getChainIdForNextBlock());
				tx.sign(senderKey);
				logger.info("<== Submitting tx: " + tx);
				ethereum.submitTransaction(tx);
			}
			Thread.sleep(7000);
		}
	}

	private void generateOneTransaction() throws Exception {
		logger.info("Start generating a transaction...");

		//PrivateKey
		//3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c
		//PublicKey
		//5db10750e8caff27f906b41c71b3471057dd2004
		
		//PrivateKey
		//6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec
		//PublicKey
		//31e2e1ed11951c7091dfba62cd4b7145e947219c
		
		//PrivateKey
		//fee3b6045d75237490f1ba055bf6d034b2a83c71c78fb526b3183b5c68944f1d
		//PublicKey
		//ee0250c19ad59305b2bdb61f34b45b72fe37154f
		
		// the sender which some coins from the genesis
		ECKey senderKey = ECKey
				.fromPrivate(Hex.decode("6ef8da380c27cea8fdf7448340ea99e8e2268fc2950d79ed47cbf6f85dc977ec"));
		byte[] receiverAddr = Hex.decode("31e2e1ed11951c7091dfba62cd4b7145e947219c");

		System.out.println("SENDER: " + ByteUtil.toHexString(senderKey.getAddress()));
		System.out.println("RECEIVER: " + ByteUtil.toHexString(receiverAddr));
		
		int i = ethereum.getRepository().getNonce(senderKey.getAddress()).intValue();
		Transaction tx = new Transaction(ByteUtil.intToBytesNoLeadZeroes(i),
				ByteUtil.longToBytesNoLeadZeroes(50_000_000_000L), ByteUtil.longToBytesNoLeadZeroes(0xfffff),
				receiverAddr, new byte[] { 77 }, new byte[0], ethereum.getChainIdForNextBlock());
		tx.sign(senderKey);
		logger.info("<== Submitting tx: " + tx);
		ethereum.submitTransaction(tx);
	}
}
