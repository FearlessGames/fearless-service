package se.fearless.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

import java.util.Optional;

public class Router {
	private final Multimap<HttpMethod, RoutePath> routes = ArrayListMultimap.create();

	public void addRoute(HttpMethod method, String path, RequestHandler<ByteBuf, ByteBuf> handler) {
		routes.put(method, new RoutePath(path, handler));
	}

	public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
		HttpMethod httpMethod = HttpMethod.fromString(request.getHttpMethod().toString());
		Optional<RoutePath> matchingRoute = routes.get(httpMethod).stream().filter(routePath -> routePath.handles(request)).findFirst();
		if (matchingRoute.isPresent()) {
			return matchingRoute.get().handler.handle(request, response);
		}
		response.setStatus(HttpResponseStatus.NOT_FOUND);
		return response.close();
	}


	private static class RoutePath {
		private final String path;
		private final RequestHandler<ByteBuf, ByteBuf> handler;

		public RoutePath(String path, RequestHandler<ByteBuf, ByteBuf> handler) {
			this.path = path;
			this.handler = handler;
		}

		boolean handles(HttpServerRequest<ByteBuf> request) {
			return path.equals(request.getPath());
		}
	}
}
