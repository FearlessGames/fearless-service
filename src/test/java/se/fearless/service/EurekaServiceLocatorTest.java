package se.fearless.service;

import com.netflix.eureka2.client.EurekaClient;
import com.netflix.eureka2.interests.ChangeNotification;
import com.netflix.eureka2.registry.InstanceInfo;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;
import se.mockachino.Mockachino;
import se.mockachino.Settings;

import static org.junit.Assert.assertEquals;
import static se.mockachino.Mockachino.mock;
import static se.mockachino.Mockachino.when;

public class EurekaServiceLocatorTest {

	@Test
	public void getWithNoServersReturnNull() throws Exception {

	}

	@Test
	public void getWithOneServerUsesThatData() throws Exception {
		String serverName = "serverName";
		Integer port = 9876;

		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = Observable.just(new EurekaServiceLocator.ServiceInfo(serverName, port, true));

		EurekaServiceLocator locator = new EurekaServiceLocator(infoObservable);
		String serviceLocation = locator.get();
		String expectedLocation = "http://" + serverName + ":" + port;
		assertEquals(expectedLocation, serviceLocation);
	}

	@Test
	public void getTwiceWithOneServerGetTheSameAddress() throws Exception {
		String serverName = "serverName";
		Integer port = 9876;

		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = Observable.just(new EurekaServiceLocator.ServiceInfo(serverName, port, true));

		EurekaServiceLocator locator = new EurekaServiceLocator(infoObservable);
		String serviceLocation1 = locator.get();
		String serviceLocation2 = locator.get();

		assertEquals(serviceLocation1, serviceLocation2);
	}
}