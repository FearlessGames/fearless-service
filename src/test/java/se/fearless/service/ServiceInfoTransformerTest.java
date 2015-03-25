package se.fearless.service;

import com.netflix.eureka2.interests.ChangeNotification;
import com.netflix.eureka2.registry.InstanceInfo;
import org.junit.Test;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServiceInfoTransformerTest {

	@Test
	public void transformServerUpEntry() throws Exception {
		String ip = "192.168.0.12";
		int port = 4711;
		InstanceInfoFactory instanceInfoFactory = new InstanceInfoFactory("app", "myService");
		InstanceInfo instanceInfo = instanceInfoFactory.create(ip, port);
		ChangeNotification<InstanceInfo> changeNotification = new ChangeNotification<>(ChangeNotification.Kind.Add, instanceInfo);

		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = ServiceInfoTransformer.transform(changeNotification);

		infoObservable.forEach(serviceInfo -> {
			System.out.println("Testing service info");
			assertEquals("http://" + ip + ":" + port, serviceInfo.getAsUrl());
			assertTrue(serviceInfo.isUp());
		});
	}

	@Test
	public void transformServerDownEntry() throws Exception {
		String ip = "192.168.0.12";
		int port = 4711;
		InstanceInfoFactory instanceInfoFactory = new InstanceInfoFactory("app", "myService");
		InstanceInfo instanceInfo = instanceInfoFactory.create(ip, port);
		ChangeNotification<InstanceInfo> changeNotification = new ChangeNotification<>(ChangeNotification.Kind.Delete, instanceInfo);

		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = ServiceInfoTransformer.transform(changeNotification);

		infoObservable.forEach(serviceInfo -> {
			System.out.println("Testing service info");
			assertEquals("http://" + ip + ":" + port, serviceInfo.getAsUrl());
			assertFalse(serviceInfo.isUp());
		});
	}

}