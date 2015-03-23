package se.fearless.service;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.junit.Before;
import org.junit.Test;
import rx.subjects.PublishSubject;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static se.mockachino.Mockachino.mock;

public class EurekaServiceLocatorTest {

	private String serverName1 = "foo";
	private Integer port1 = 9876;

	private String serverName2 = "bar";
	private Integer port2 = 7652;

	private PublishSubject<EurekaServiceLocator.ServiceInfo> subject;
	private EurekaServiceLocator serviceLocator;

	@Before
	public void setUp() throws Exception {
		subject = PublishSubject.create();
		serviceLocator = new EurekaServiceLocator(subject);
	}

	@Test
	public void getWithOneServerUsesThatData() throws Exception {
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName1, port1, true));
		String serviceLocation = serviceLocator.get();
		String expectedLocation = buildUrlFromServerNameAndPort(serverName1, port1);
		assertEquals(expectedLocation, serviceLocation);
	}

	private static String buildUrlFromServerNameAndPort(String serverName1, Integer port1) {
		return "http://" + serverName1 + ":" + port1;
	}

	@Test
	public void getTwiceWithOneServerGetTheSameAddress() throws Exception {
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName1, port1, true));

		String serviceLocation1 = serviceLocator.get();
		String serviceLocation2 = serviceLocator.get();

		assertEquals(serviceLocation1, serviceLocation2);
	}

	@Test
	public void getTwiceWithTwoServersGetDifferentAddresses() throws Exception {
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName1, port1, true));
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName2, port2, true));

		String serviceLocation1 = serviceLocator.get();
		String serviceLocation2 = serviceLocator.get();

		assertNotEquals(serviceLocation1, serviceLocation2);
	}

	@Test
	public void getWhenOneServiceWasAddedAndThenRemoved() throws Exception {
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName1, port1, true));
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName2, port2, true));
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName1, port1, false));

		String location = serviceLocator.get();
		String expectedLocation = buildUrlFromServerNameAndPort(serverName2, port2);
		assertEquals(expectedLocation, location);
	}

	@Test
	public void getTenTimesWithTwoServersGetFiveTimesTwoDifferentAddresses() throws Exception {
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName1, port1, true));
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName2, port2, true));

		Multiset<String> locations = HashMultiset.create();

		// when
		for (int i = 0; i < 10; i++) {
			String location = serviceLocator.get();
			locations.add(location);
		}

		thenTheTwoServersAreReturnedTheSameNumberOfTimes(locations);
	}

	private void thenTheTwoServersAreReturnedTheSameNumberOfTimes(Multiset<String> locations) {
		assertEquals(2, locations.elementSet().size());

		Set<Multiset.Entry<String>> entries = locations.entrySet();
		for (Multiset.Entry<String> entry : entries) {
			assertEquals(5, entry.getCount());
		}
	}
}