package se.fearless.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class HostnameProvider {
	public String get() {
		String hostAddress = "unknown";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			hostAddress = "unknown-" + UUID.randomUUID();
		}
		return hostAddress;
	}
}
