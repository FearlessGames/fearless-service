package se.fearless.service;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import org.junit.Before;
import org.junit.Test;
import se.fearless.rxtestutils.HttpRequestMocks;

import java.util.regex.Pattern;

import static se.mockachino.Mockachino.*;

public class RouterTest {

	private Router router;

	private RequestHandler<ByteBuf, ByteBuf> handler;
	private HttpServerRequest<ByteBuf> request;
	private HttpServerResponse<ByteBuf> response;


	@Before
	public void setUp() throws Exception {
		router = new Router();
		handler = HttpRequestMocks.requestHandler();
		request = HttpRequestMocks.request();
		response = HttpRequestMocks.response();
	}

	@Test
	public void whenCalledWithExactMatchStaticRoutingInvokesHandler() throws Exception {
		setupGetForPath("/foo");

		router.addRoute(HttpMethod.GET, "/foo", handler);
		router.route(request, response);

		verifyOnce().on(handler).handle(request, response);
	}

	@Test
	public void whenCalledWithMismatchStaticRoutingReturns404() throws Exception {
		setupGetForPath("/bar");

		router.addRoute(HttpMethod.GET, "/foo", handler);
		router.route(request, response);

		verifyFail();
	}

	@Test
	public void whenCalledWithMatchingExpressionRoutingInvokesHandler() throws Exception {
		setupGetForPath("/match23");

		router.addRoute(HttpMethod.GET, Pattern.compile("/match[\\d]+"), handler);
		router.route(request, response);

		verifyOnce().on(handler).handle(request, response);
	}

	@Test
	public void whenCalledWithNonMatchingExpressionRoutingReturns404() throws Exception {
		setupGetForPath("/matching");

		router.addRoute(HttpMethod.GET, Pattern.compile("/match[\\d]+"), handler);
		router.route(request, response);

		verifyFail();
	}

	private void setupGetForPath(String value) {
		when(request.getHttpMethod()).thenReturn(io.netty.handler.codec.http.HttpMethod.GET);
		when(request.getPath()).thenReturn(value);
	}

	private void verifyFail() {
		verifyNever().on(handler).handle(request, response);
		verifyOnce().on(response).setStatus(HttpResponseStatus.NOT_FOUND);
	}
}