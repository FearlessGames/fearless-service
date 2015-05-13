package se.fearless.service;

import com.netflix.eureka2.client.Eureka;
import com.netflix.eureka2.client.EurekaClient;
import com.netflix.eureka2.client.resolver.ServerResolver;
import com.netflix.eureka2.client.resolver.ServerResolvers;
import com.netflix.eureka2.interests.ChangeNotification;
import com.netflix.eureka2.registry.InstanceInfo;
import com.netflix.eureka2.registry.NetworkAddress;
import com.netflix.eureka2.registry.ServicePort;
import com.netflix.eureka2.registry.datacenter.BasicDataCenterInfo;
import com.netflix.eureka2.transport.EurekaTransports;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MicroService {
	private final int port;
	private final Router router;
	private final String systemName;
	private final String serviceName;
	private final EurekaServerInfo eurekaServerInfo;
	private final HostnameProvider hostnameProvider;
	private HttpServer<ByteBuf, ByteBuf> httpServer;
	private EurekaClient client;

	public MicroService(int port, Router router, String systemName, String serviceName, EurekaServerInfo eurekaServerInfo, HostnameProvider hostnameProvider) {
		this.port = port;
		this.router = router;
		this.systemName = systemName;
		this.serviceName = serviceName;
		this.eurekaServerInfo = eurekaServerInfo;
		this.hostnameProvider = hostnameProvider;
	}


	public void start() {
		httpServer = RxNetty.createHttpServer(port, router::route);
		client = Eureka.newClientBuilder(eurekaServerInfo.getEurekaDiscovery(), eurekaServerInfo.getEurekaRegistry()).
				withCodec(EurekaTransports.Codec.Json).build();



		client.register(buildInstanceInfo(InstanceInfo.Status.UP)).doOnCompleted(
				() -> System.out.println("Registered in eureka")).toBlocking().lastOrDefault(null);
		httpServer.start();
	}

	private InstanceInfo buildInstanceInfo(InstanceInfo.Status status) {
		BasicDataCenterInfo location = BasicDataCenterInfo.fromSystemData();
		BasicDataCenterInfo basicDataCenterInfo = new BasicDataCenterInfo(hostnameProvider.get(), Collections.singletonList(getLocalIpv4Address(location)));
		return new InstanceInfo.Builder()
				.withId(hostnameProvider.get() + ":" + port)
				.withApp(systemName)
				.withStatus(status)
				.withVipAddress(serviceName)

				.withPorts(new ServicePort(port, false))
				.withDataCenterInfo(basicDataCenterInfo)
				.build();
	}

	private NetworkAddress getLocalIpv4Address(BasicDataCenterInfo location) {
		List<NetworkAddress> ipv4Adresses = location.getAddresses().stream().filter(networkAddress -> networkAddress.getProtocolType() == NetworkAddress.ProtocolType.IPv4).collect(Collectors.toList());
		Optional<NetworkAddress> firstPublic = ipv4Adresses.stream().filter(networkAddress -> networkAddress.getLabel().equals(NetworkAddress.PUBLIC_ADDRESS)).findFirst();
		if (firstPublic.isPresent()) {
			return firstPublic.get();
		}
		Optional<NetworkAddress> firstIp4 = ipv4Adresses.stream().findFirst();
		if (firstIp4.isPresent()) {
			return firstIp4.get();
		}
		return location.getDefaultAddress();
	}

	public void waitTillShutdown() throws InterruptedException {
		httpServer.waitTillShutdown();
	}

	public void stop() {
		try {
			client.update(buildInstanceInfo(InstanceInfo.Status.DOWN)).toBlocking().lastOrDefault(null);
			httpServer.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ServiceLocator getServiceLocator(String serviceName) {
		Observable<ChangeNotification<InstanceInfo>> changeNotificationObservable = client.forVips(serviceName);
		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = changeNotificationObservable.flatMap(ServiceInfoTransformer::transform);

		return new EurekaServiceLocator(infoObservable);
	}


	public static class Builder {
		private int port = 0;
		private EurekaServerInfo eurekaServerInfo = new EurekaServerInfo(ServerResolvers.from(new ServerResolver.Server("localhost", 2222)),
				ServerResolvers.from(new ServerResolver.Server("localhost", 2223)));
		private final Router router;
		private final String systemName;
		private final String serviceName;
		private HostnameProvider hostnameProvider = new HostnameProvider();


		public Builder(Router router, String systemName, String serviceName) {
			this.router = router;
			this.systemName = systemName;
			this.serviceName = serviceName;
		}

		public Builder withPort(int port) {
			this.port = port;
			return this;
		}

		public Builder withEurekaServerInfo(EurekaServerInfo eurekaServerInfo) {
			this.eurekaServerInfo = eurekaServerInfo;
			return this;
		}

		public Builder withHostnameProvider(HostnameProvider hostnameProvider) {
			this.hostnameProvider = hostnameProvider;
			return this;
		}

		public MicroService build() {
			if (port == 0) {
				port = findFreePort();
			}
			return new MicroService(port, router, systemName, serviceName, eurekaServerInfo, hostnameProvider);
		}

		private int findFreePort() {
			try (ServerSocket socket = new ServerSocket(0)) {
				return socket.getLocalPort();
			} catch (IOException ignored) {
				return 0;
			}
		}

	}

}
