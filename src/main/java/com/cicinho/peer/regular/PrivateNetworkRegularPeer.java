package com.cicinho.peer.regular;

import org.ethereum.config.SystemProperties;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.samples.BasicSample;
import org.springframework.context.annotation.Bean;

import com.cicinho.peer.node.RegularNode;
import com.typesafe.config.ConfigFactory;

public class PrivateNetworkRegularPeer {

	private static String logger = "RegularPeer1";

	/**
	 * Spring configuration class for the Regular peer
	 */
	private static class RegularConfig {
		private final String config =
				// no discovery: we are connecting directly to the miner peer
				"peer.discovery.enabled = false \n" + "peer.listen.port = 30336 \n"
						+ "peer.privateKey = 3ec771c31cac8c0dba77a69e503765701d3c2bb62435888d4ffa38fed60c445c \n"
						+ "peer.networkId = 15 \n" +
						// actively connecting to the miner
						"peer.active = ["
						+ "    { url = 'enode://26ba1aadaf59d7607ad7f437146927d79e80312f026cfa635c6b2ccf2c5d3521f5812ca2beb3b295b14f97110e6448c1c7ff68f14c5328d43a3c62b44143e9b1@localhost:30335' }"
						+ "] \n" + "sync.enabled = true \n" +
						// all peers in the same network need to use the same genesis block
						"genesis = genesis.json \n" +
						// two peers need to have separate database dirs
						"database.dir = database \n";

		@Bean
		public RegularNode node() {
			return new RegularNode(logger);
		}

		/**
		 * Instead of supplying properties via config file for the peer we are
		 * substituting the corresponding bean which returns required config for this
		 * instance.
		 */
		@Bean
		public SystemProperties systemProperties() {
			SystemProperties props = new SystemProperties();
			props.overrideParams(ConfigFactory.parseString(config.replaceAll("'", "\"")));
			return props;
		}
	}

	public static void initRegularPeer() {
		BasicSample.sLogger.info("Starting EthtereumJ regular instance!");
		EthereumFactory.createEthereum(RegularConfig.class);
	}
}
