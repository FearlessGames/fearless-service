package se.fearless.service;

import com.netflix.eureka2.interests.ChangeNotification;
import com.netflix.eureka2.registry.InstanceInfo;
import org.junit.Test;
import rx.Observable;

import static org.junit.Assert.assertEquals;

public class ServiceInfoTransformerTest {

	@Test
	public void transformEntry() throws Exception {
		String ip = "192.168.0.12";
		int port = 4711;
		InstanceInfoFactory instanceInfoFactory = new InstanceInfoFactory("app", "myService");
		InstanceInfo instanceInfo = instanceInfoFactory.create(ip, port);
		ChangeNotification<InstanceInfo> changeNotification = new ChangeNotification<>(ChangeNotification.Kind.Add, instanceInfo);

		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = ServiceInfoTransformer.transform(changeNotification);

		infoObservable.forEach(serviceInfo -> {
			System.out.println("Testing service info");
			assertEquals("http://" + ip + ":" + port, serviceInfo.getAsUrl());
		});
	}

}