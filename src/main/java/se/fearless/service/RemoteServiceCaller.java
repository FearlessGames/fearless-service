package se.fearless.service;

import io.netty.buffer.ByteBuf;
import rx.Observable;
import rx.functions.Func1;

public interface RemoteServiceCaller {
	<T> Observable<T> callService(String serviceName, String path, Func1<ByteBuf, T> resultMapper);
}
