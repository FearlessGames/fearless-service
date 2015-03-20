package se.fearless.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
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

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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

	@Test
	public void getTwiceWithTwoServersGetDifferentAddresses() throws Exception {
		String serverName1 = "foo";
		String serverName2 = "bar";
		Integer port1 = 9876;
		Integer port2 = 7652;

		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = Observable.just(
				new EurekaServiceLocator.ServiceInfo(serverName1, port1, true),
				new EurekaServiceLocator.ServiceInfo(serverName2, port2, true));

		EurekaServiceLocator locator = new EurekaServiceLocator(infoObservable);
		String serviceLocation1 = locator.get();
		String serviceLocation2 = locator.get();

		assertNotEquals(serviceLocation1, serviceLocation2);
	}

	@Test
	public void getTenTimesWithTwoServersGetFiveTimesTwoDifferentAddresses() throws Exception {
		String serverName1 = "foo";
		String serverName2 = "bar";
		Integer port1 = 9876;
		Integer port2 = 7652;

		Observable<EurekaServiceLocator.ServiceInfo> infoObservable = Observable.just(
				new EurekaServiceLocator.ServiceInfo(serverName1, port1, true),
				new EurekaServiceLocator.ServiceInfo(serverName2, port2, true));

		EurekaServiceLocator locator = new EurekaServiceLocator(infoObservable);

		Multiset<String> locations = HashMultiset.create();

		for (int i = 0; i < 10; i++) {
			String location = locator.get();
			locations.add(location);
		}

		assertEquals(2, locations.elementSet().size());

		Set<Multiset.Entry<String>> entries = locations.entrySet();
		for (Multiset.Entry<String> entry : entries) {
			assertEquals(5, entry.getCount());
		}
	}
}