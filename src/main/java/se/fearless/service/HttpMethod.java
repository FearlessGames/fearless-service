package se.fearless.service;

public enum HttpMethod {
	GET,
	POST,
	DELETE;

	public static HttpMethod fromString(String name) {
		return HttpMethod.valueOf(name.toUpperCase());
	}
}
