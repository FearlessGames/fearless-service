package se.fearless.service;

import com.netflix.eureka2.registry.InstanceInfo;
import com.netflix.eureka2.registry.NetworkAddress;
import com.netflix.eureka2.registry.ServicePort;
import com.netflix.eureka2.registry.datacenter.BasicDataCenterInfo;

public class InstanceInfoFactory {

	private final String appName;
	private final String vip;
	private long id = -1L;

	public InstanceInfoFactory(String appName, String serviceName) {
		this.appName = appName;
		vip = serviceName;
	}

	InstanceInfo create(String ip, int port) {
		NetworkAddress address = NetworkAddress.NetworkAddressBuilder.aNetworkAddress().withIpAddress(ip).withProtocolType(NetworkAddress.ProtocolType.IPv4).withLabel(NetworkAddress.PUBLIC_ADDRESS).build();
		BasicDataCenterInfo.BasicDataCenterInfoBuilder dataCenterInfoBuilder = new BasicDataCenterInfo.BasicDataCenterInfoBuilder();
		dataCenterInfoBuilder.withAddresses(address);
		ServicePort servicePort = new ServicePort(port, false);
		id++;
		return new InstanceInfo.Builder().withId(String.valueOf(id)).withApp(appName).withVipAddress(vip).withDataCenterInfo(dataCenterInfoBuilder.build()).withPorts(servicePort).build();
	}
}
