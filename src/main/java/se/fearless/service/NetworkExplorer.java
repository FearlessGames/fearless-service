package se.fearless.service;

import com.netflix.eureka2.utils.SystemUtil;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.util.List;

public class NetworkExplorer {

	public static void main(String[] args) {

		System.out.println("Local IPs");
		List<String> localIPs = SystemUtil.getLocalIPs();
		localIPs.stream().forEach(System.out::println);

		System.out.println("\nPublic IPs");
		List<String> publicIPs = SystemUtil.getPublicIPs();
		publicIPs.stream().forEach(System.out::println);

		System.out.println("\nPrivate IPs");
		List<String> privateIPs = SystemUtil.getPrivateIPs();
		privateIPs.stream().forEach(System.out::println);
	}

}
