package se.fearless.service;

import com.netflix.eureka2.interests.ChangeNotification;
import com.netflix.eureka2.registry.InstanceInfo;
import org.junit.Test;
import rx.Observable;

public class ServiceInfoTransformerTest {

	@Test
	public void transformSingleEntry() throws Exception {
		ServiceInfoTransformer serviceInfoTransformer = new ServiceInfoTransformer();
		InstanceInfo instanceInfo = new InstanceInfo.Builder().build();
		ChangeNotification<InstanceInfo> changeNotification = new ChangeNotification<>(ChangeNotification.Kind.Add, instanceInfo);
		Observable<ChangeNotification<InstanceInfo>> changeNotificationObservable = Observable.just(changeNotification);
		//Observable<EurekaServiceLocator.ServiceInfo> infoObservable = serviceInfoTransformer.transform(changeNotificationObservable);

	}
}