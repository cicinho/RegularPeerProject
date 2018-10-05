package com.cicinho.peer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cicinho.peer.regular.PrivateNetworkRegularPeer;

@SpringBootApplication
public class RegularPeerProjectApplication {	
		
	public static void main(String[] args) {
		SpringApplication.run(RegularPeerProjectApplication.class, args);
	
		PrivateNetworkRegularPeer.initRegularPeer();
	}
}
