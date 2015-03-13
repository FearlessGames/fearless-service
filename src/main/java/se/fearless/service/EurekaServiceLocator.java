package se.fearless.service;

import com.netflix.eureka2.client.EurekaClient;
import com.netflix.eureka2.registry.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EurekaServiceLocator implements ServiceLocator {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String serviceName;
	private final EurekaClient client;
	private final List<InstanceInfo.ServiceEndpoint> endpoints = new ArrayList<>();

	public EurekaServiceLocator(String serviceName, EurekaClient client) {
		this.serviceName = serviceName;
		this.client = client;
		client.forVips(serviceName).forEach(notification -> {
			logger.debug("Got notification " + notification);
			notification.getData().serviceEndpoints().forEachRemaining((serviceEndpoint) -> {
				logger.debug("Adding endpoint " + serviceEndpoint.getAddress() + ":" + serviceEndpoint.getServicePort());
				endpoints.add(serviceEndpoint);
			});
		});
	}

	@Override
	public String get() {
		if (endpoints.isEmpty()) {
			return null;
		}
		InstanceInfo.ServiceEndpoint serviceEndpoint = endpoints.get(0);

		return "http://" + serviceEndpoint.getAddress().getIpAddress() + ":" + serviceEndpoint.getServicePort().getPort();

	}
}
