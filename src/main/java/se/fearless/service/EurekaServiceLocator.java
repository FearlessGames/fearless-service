package se.fearless.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EurekaServiceLocator implements ServiceLocator {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final List<ServiceInfo> endpoints = new ArrayList<>();
	private int currentIndex = 0;

	public EurekaServiceLocator(Observable<ServiceInfo> serviceInfoObservable) {
		serviceInfoObservable.forEach(notification -> {
			logger.debug("Got notification " + notification);
			if (notification.up) {
				endpoints.add(notification);
			} else {
				endpoints.remove(notification);
			}
		});
	}

	@Override
	public String get() {
		if (endpoints.isEmpty()) {
			return null;
		}
		ServiceInfo serviceInfo = endpoints.get(currentIndex);
		currentIndex++;
		currentIndex = currentIndex == endpoints.size() ? 0: currentIndex;
		return serviceInfo.getAsUrl();
	}

	public static class ServiceInfo {
		private final String serverAdress;

		private final int port;
		private final boolean up;

		public ServiceInfo(String serverAdress, int port, boolean up) {
			this.serverAdress = serverAdress;
			this.port = port;
			this.up = up;
		}

		public String getAsUrl() {
			return "http://" + serverAdress + ":" + port;
		}

		@Override
		public String toString() {
			return "ServiceInfo{ " + serverAdress + ":" + port + " - " + (up ? "UP" : "DOWN") + " }";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ServiceInfo that = (ServiceInfo) o;
			return Objects.equals(port, that.port) &&
					Objects.equals(serverAdress, that.serverAdress);
		}

		@Override
		public int hashCode() {
			return Objects.hash(serverAdress, port);
		}
	}
}
