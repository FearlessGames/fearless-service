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
		postInfoToSubject(subject, serverName1, port1, true);
		String serviceLocation = serviceLocator.get();
		String expectedLocation = buildUrlFromServerNameAndPort(serverName1, port1);
		assertEquals(expectedLocation, serviceLocation);
	}

	@Test
	public void getTwiceWithOneServerGetTheSameAddress() throws Exception {
		postInfoToSubject(subject, serverName1, port1, true);

		String serviceLocation1 = serviceLocator.get();
		String serviceLocation2 = serviceLocator.get();

		assertEquals(serviceLocation1, serviceLocation2);
	}

	@Test
	public void getTwiceWithTwoServersGetDifferentAddresses() throws Exception {
		postInfoToSubject(subject, serverName1, port1, true);
		postInfoToSubject(subject, serverName2, port2, true);

		String serviceLocation1 = serviceLocator.get();
		String serviceLocation2 = serviceLocator.get();

		assertNotEquals(serviceLocation1, serviceLocation2);
	}

	@Test
	public void getWhenOneServiceWasAddedAndThenRemoved() throws Exception {
		postInfoToSubject(subject, serverName1, port1, true);
		postInfoToSubject(subject, serverName2, port2, true);
		postInfoToSubject(subject, serverName1, port1, false);

		String location = serviceLocator.get();
		String expectedLocation = buildUrlFromServerNameAndPort(serverName2, port2);
		assertEquals(expectedLocation, location);
	}

	@Test
	public void getTenTimesWithTwoServersGetFiveTimesTwoDifferentAddresses() throws Exception {
		postInfoToSubject(subject, serverName1, port1, true);
		postInfoToSubject(subject, serverName2, port2, true);

		Multiset<String> locations = HashMultiset.create();

		// when
		for (int i = 0; i < 10; i++) {
			String location = serviceLocator.get();
			locations.add(location);
		}

		thenTheTwoServersAreReturnedTheSameNumberOfTimes(locations, 2, 5);
	}

	@Test
	public void getAFewTimesAfterServicesHaveGoneUpAndDown() throws Exception {
		String serverName3 = "baz";
		Integer port3 = 5763;
		postInfoToSubject(subject, serverName1, port1, true);
		postInfoToSubject(subject, serverName2, port2, true);
		postInfoToSubject(subject, serverName3, port3, true);

		serviceLocator.get();
		serviceLocator.get();

		postInfoToSubject(subject, serverName2, port2, false);

		Multiset<String> locations = HashMultiset.create();
		for (int i = 0; i < 10; i++) {
			String location = serviceLocator.get();
			locations.add(location);
		}

		thenTheTwoServersAreReturnedTheSameNumberOfTimes(locations, 2, 5);
	}

	private static void thenTheTwoServersAreReturnedTheSameNumberOfTimes(Multiset<String> locations, int expectedNumberOfServers, int expectedEntriesForEach) {
		assertEquals(expectedNumberOfServers, locations.elementSet().size());

		Set<Multiset.Entry<String>> entries = locations.entrySet();
		for (Multiset.Entry<String> entry : entries) {
			assertEquals(expectedEntriesForEach, entry.getCount());
		}
	}

	private static String buildUrlFromServerNameAndPort(String serverName, Integer port) {
		return "http://" + serverName + ":" + port;
	}

	private static void postInfoToSubject(PublishSubject<EurekaServiceLocator.ServiceInfo> subject, String serverName1, Integer port1, boolean up) {
		subject.onNext(new EurekaServiceLocator.ServiceInfo(serverName1, port1, up));
	}
}