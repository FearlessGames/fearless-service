package se.fearless.service;

import com.netflix.eureka2.client.Eureka;
import com.netflix.eureka2.client.EurekaClient;
import com.netflix.eureka2.registry.InstanceInfo;
import com.netflix.eureka2.registry.ServicePort;
import com.netflix.eureka2.registry.datacenter.BasicDataCenterInfo;
import com.netflix.eureka2.transport.EurekaTransports;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.functions.Action0;

import java.util.Arrays;

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

		BasicDataCenterInfo location = BasicDataCenterInfo.fromSystemData();
		BasicDataCenterInfo basicDataCenterInfo = new BasicDataCenterInfo(hostnameProvider.get(), Arrays.asList(location.getDefaultAddress()));

		client.register(new InstanceInfo.Builder()
				.withId(hostnameProvider.get() + ":" + port)
				.withApp(systemName)
				.withStatus(InstanceInfo.Status.UP)
				.withVipAddress(serviceName)

				.withPorts(new ServicePort(port, false))
				.withDataCenterInfo(basicDataCenterInfo)
				.build()).doOnCompleted(new Action0() {
			@Override
			public void call() {
				System.out.println("Registered in eureka");
			}
		}).toBlocking().lastOrDefault(null);
		httpServer.start();
	}

	public void waitTillShutdown() throws InterruptedException {
		httpServer.waitTillShutdown();
	}

	public void stop() {
		try {
			httpServer.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ServiceLocator getServiceLocator(String serviceName) {
		return new EurekaServiceLocator(serviceName, client);
	}
}
