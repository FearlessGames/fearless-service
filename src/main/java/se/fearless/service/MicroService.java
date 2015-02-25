package se.fearless.service;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.RequestHandler;

public class MicroService {
	private final int port;
	private final RequestHandler<ByteBuf, ByteBuf> requestHandler;
	private HttpServer<ByteBuf, ByteBuf> httpServer;

	public MicroService(int port, RequestHandler<ByteBuf, ByteBuf> requestHandler) {
		this.port = port;
		this.requestHandler = requestHandler;
	}

	public void start() {
		httpServer = RxNetty.createHttpServer(port, requestHandler);
		httpServer.start();
	}

	public void stop() {
		try {
			httpServer.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
