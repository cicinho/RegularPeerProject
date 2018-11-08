package com.cicinho.peer.regular;

import java.io.IOException;
import java.util.Properties;

import org.ethereum.config.SystemProperties;
import org.ethereum.facade.EthereumFactory;
import org.ethereum.samples.BasicSample;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.cicinho.peer.node.RegularNode;
import com.typesafe.config.ConfigFactory;

public class PrivateNetworkRegularPeer {
	
	static Properties properties;

	/**
	 * Spring configuration class for the Regular peer
	 */
	private static class RegularConfig {
		
		private final String config =
				// no discovery: we are connecting directly to the miner peer
				"peer.discovery.enabled = " + properties.getProperty("peer.discovery.enabled") + " \n" + 
						"peer.listen.port = " + properties.getProperty("peer.listen.port") + " \n"
						+ "peer.privateKey = " + properties.getProperty("peer.privateKey") + " \n"
						+ "peer.networkId = " + properties.getProperty("peer.networkId") + " \n" +
						// actively connecting to the miner
						"peer.active = ["
						+ "    { url = '" + properties.getProperty("enode.miner1") + "' }"
						+ "] \n" 
						+ "sync.enabled = " + properties.getProperty("sync.enabled") + " \n" +
						// all peers in the same network need to use the same genesis block
						"genesis = " + properties.getProperty("genesis") + " \n" +
						// two peers need to have separate database dirs
						"database.dir = " + properties.getProperty("database.dir") + " \n"
						+ "peer.discovery.external.ip = " + properties.getProperty("peer.discovery.external.ip");

		@Bean
		public RegularNode node() {
			return new RegularNode(properties.getProperty("node.name"));
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
		try {
			properties = PropertiesLoaderUtils.loadAllProperties("application.properties");
		} catch (IOException e) {			
		}
		BasicSample.sLogger.info("Starting EthtereumJ regular instance!");
		EthereumFactory.createEthereum(RegularConfig.class);
	}
}
