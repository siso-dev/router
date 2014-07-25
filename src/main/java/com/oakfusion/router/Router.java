package com.oakfusion.router;

import com.oakfusion.router.util.UriTree;

import java.lang.reflect.Method;

import static java.lang.String.format;

public class Router {

	public static final Route R_404 = new Route("", "", null, null);

	protected RouteContext ctx;

	private final HttpMethodBuilder httpMethodBuilder = new HttpMethodBuilder();
	private final HandlerClassBuilder handlerClassBuilder = new HandlerClassBuilder();
	private final HandlerMethodBuilder handlerMethodBuilder = new HandlerMethodBuilder();

	private final  MethodLoader methodLoader = new MethodLoader();
	private final UriTree<Route> routes = new UriTree<>("/");

	public HttpMethodBuilder route(String uri) {
		ctx = new RouteContext();
		ctx.uri = uri;
		return httpMethodBuilder;
	}

	public Route getRouteFor(String httpMethod, String uri) {
		String path = format("/%s%s", httpMethod, uri);
		UriTree<Route> node = routes.get(path);
		if (node == null) {
			return R_404;
		}
		return node.getData();
	}

	protected class RouteContext {
		public String uri;
		public String httpMethod;
		public Class<?> controllerClass;
		public String controllerMethodName;
	}

	private RouteContext getContext() {
		return ctx;
	}

	private Router completeChain() {
		try {
			Method method = methodLoader.load(ctx.controllerClass, ctx.controllerMethodName);
			Route route = new Route(ctx.httpMethod, ctx.uri, ctx.controllerClass, method);
			routes.put(ctx.httpMethod).put(ctx.uri, route);
			return this;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private enum HttpMethod {
		// http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
	}

	public class HttpMethodBuilder {
		public HandlerClassBuilder whenOPTIONS() {
			return when(HttpMethod.OPTIONS);
		}

		public HandlerClassBuilder whenGET() {
			return when(HttpMethod.GET);
		}

		public HandlerClassBuilder whenHEAD() {
			return when(HttpMethod.HEAD);
		}

		public HandlerClassBuilder whenPOST() {
			return when(HttpMethod.POST);
		}

		public HandlerClassBuilder whenPUT() {
			return when(HttpMethod.PUT);
		}

		public HandlerClassBuilder whenDELETE() {
			return when(HttpMethod.DELETE);
		}

		public HandlerClassBuilder whenTRACE() {
			return when(HttpMethod.TRACE);
		}

		public HandlerClassBuilder whenCONNECT() {
			return when(HttpMethod.CONNECT);
		}

		private HandlerClassBuilder when(HttpMethod httpMethod) {
			return when(httpMethod.name());
		}

		public HandlerClassBuilder when(String httpMethod) {
			getContext().httpMethod = httpMethod;
			return handlerClassBuilder;
		}
	}

	public HandlerClassBuilder whenOPTIONS() {
		return when(HttpMethod.OPTIONS);
	}

	public HandlerClassBuilder whenGET() {
		return when(HttpMethod.GET);
	}

	public HandlerClassBuilder whenHEAD() {
		return when(HttpMethod.HEAD);
	}

	public HandlerClassBuilder whenPOST() {
		return when(HttpMethod.POST);
	}

	public HandlerClassBuilder whenPUT() {
		return when(HttpMethod.PUT);
	}

	public HandlerClassBuilder whenDELETE() {
		return when(HttpMethod.DELETE);
	}

	public HandlerClassBuilder whenTRACE() {
		return when(HttpMethod.TRACE);
	}

	public HandlerClassBuilder whenCONNECT() {
		return when(HttpMethod.CONNECT);
	}

	private HandlerClassBuilder when(HttpMethod httpMethod) {
		return when(httpMethod.name());
	}

	public HandlerClassBuilder when(String httpMethod) {
		if (getContext() == null) {
			throw new RuntimeException("Route context not initialized");
		}
		return httpMethodBuilder.when(httpMethod);
	}

	public class HandlerClassBuilder {
		public HandlerMethodBuilder handleIn(Class<?> controllerClass) {
			getContext().controllerClass = controllerClass;
			return handlerMethodBuilder;
		}
	}

	public class HandlerMethodBuilder {
		public Router by(String methodName) {
			getContext().controllerMethodName = methodName;
			return completeChain();
		}
	}

}
