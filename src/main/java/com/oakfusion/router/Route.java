package com.oakfusion.router;

import java.lang.reflect.Method;

public class Route {

	public final String httpMethod;
	public final String uri;
	public final Class<?> controllerClass;
	public final Method controllerMethod;

	public Route(String httpMethod, String uri, Class<?> controllerClass, Method controllerMethod) {
		this.httpMethod = httpMethod;
		this.uri = uri;
		this.controllerClass = controllerClass;
		this.controllerMethod = controllerMethod;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public String getUri() {
		return uri;
	}

	public Class<?> getControllerClass() {
		return controllerClass;
	}

	public Method getControllerMethod() {
		return controllerMethod;
	}

}
