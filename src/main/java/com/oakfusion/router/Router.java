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

	private RouteContext getCtx() {
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
		GET, POST, PUT, DELETE, TRACE
	}

	public class HttpMethodBuilder {
		public HandlerClassBuilder whenGET() {
			return when(HttpMethod.GET);
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

		private HandlerClassBuilder when(HttpMethod httpMethod) {
			return when(httpMethod.name());
		}

		public HandlerClassBuilder when(String httpMethod) {
			getCtx().httpMethod = httpMethod;
			return handlerClassBuilder;
		}
	}

	public class HandlerClassBuilder {
		public HandlerMethodBuilder handleIn(Class<?> controllerClass) {
			getCtx().controllerClass = controllerClass;
			return handlerMethodBuilder;
		}
	}

	public class HandlerMethodBuilder {
		public Router by(String methodName) {
			getCtx().controllerMethodName = methodName;
			return completeChain();
		}
	}

}
