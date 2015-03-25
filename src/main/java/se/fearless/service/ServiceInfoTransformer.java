package se.fearless.service;

import com.netflix.eureka2.interests.ChangeNotification;
import com.netflix.eureka2.registry.InstanceInfo;
import com.netflix.eureka2.registry.NetworkAddress;
import rx.Observable;

public class ServiceInfoTransformer {
	public static Observable<EurekaServiceLocator.ServiceInfo> transform(ChangeNotification<InstanceInfo> notification) {
		InstanceInfo data = notification.getData();
		NetworkAddress defaultAddress = data.getDataCenterInfo().getDefaultAddress();
		return Observable.from(data.getPorts()).map(servicePort -> {
			boolean isUp = notification.getKind() == ChangeNotification.Kind.Add;
			return new EurekaServiceLocator.ServiceInfo(defaultAddress.getIpAddress(), servicePort.getPort(), isUp);
		});
	}

}
