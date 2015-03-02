package se.fearless.service;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;

public class MicroService {
	private final int port;
	private final Router router;
	private HttpServer<ByteBuf, ByteBuf> httpServer;

	public MicroService(int port, Router router) {
		this.port = port;
		this.router = router;
	}

	public void start() {
		httpServer = RxNetty.createHttpServer(port, router::route);
		httpServer.startAndWait();
	}

	public void stop() {
		try {
			httpServer.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
