package se.fearless.service;

import com.netflix.eureka2.client.resolver.ServerResolver;

public class EurekaServerInfo {
	private final ServerResolver eurekaDiscovery;
	private final ServerResolver eurekaRegistry;

	public EurekaServerInfo(ServerResolver eurekaDiscovery, ServerResolver eurekaRegistry) {
		this.eurekaDiscovery = eurekaDiscovery;
		this.eurekaRegistry = eurekaRegistry;
	}

	public ServerResolver getEurekaDiscovery() {
		return eurekaDiscovery;
	}

	public ServerResolver getEurekaRegistry() {
		return eurekaRegistry;
	}
}
