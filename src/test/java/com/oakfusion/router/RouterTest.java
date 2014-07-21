package com.oakfusion.router;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RouterTest {

	public static final String URI_TO_RESOURCE = "/uri/to/resource";
	public static final String CUSTOM_HTTP_METHOD = "CUSTOM_HTTP_METHOD";
	public static final String METHOD_NAME = "simpleCall";

	private Router router = new Router();

	@Test
	public void should_context_not_be_initialized() {
		// then
		assertThat(router.ctx).isNull();
	}

	@Test
	public void should_add_uri_to_context() {
		// when
		router.route(URI_TO_RESOURCE);

		// then
		assertThat(router.ctx.uri).isEqualTo(URI_TO_RESOURCE);
	}

	@Test
	public void should_add_standard_HTTP_method_to_context() {
		// when
		router.route(URI_TO_RESOURCE).whenGET();

		// then
		assertThat(router.ctx.httpMethod).isEqualTo("GET");
	}

	@Test
	public void should_add_custom_HTTP_method_to_context() {
		// when
		router.route(URI_TO_RESOURCE).when(CUSTOM_HTTP_METHOD);

		// then
		assertThat(router.ctx.httpMethod).isEqualTo(CUSTOM_HTTP_METHOD);
	}

	@Test
	public void should_add_controller_class_to_context() {
		// when
		router.route(URI_TO_RESOURCE).whenGET().handleIn(SampleController.class);

		// then
		assertThat(router.ctx.controllerClass).isEqualTo(SampleController.class);
	}

	@Test
	public void should_add_controller_method_to_context() {
		// when
		router.route(URI_TO_RESOURCE).whenGET().handleIn(SampleController.class).by(METHOD_NAME);

		// then
		assertThat(router.ctx.controllerMethodName).isEqualTo(METHOD_NAME);
	}

	@Test
	public void should_complete_main_chain() {
		// when
		router.route(URI_TO_RESOURCE).whenGET().handleIn(SampleController.class).by(METHOD_NAME);

		// then
		Route route = router.getRouteFor("GET", URI_TO_RESOURCE);
		assertThat(route).isNotNull();
		assertThat(route.getHttpMethod()).isEqualTo("GET");
		assertThat(route.getUri()).isEqualTo(URI_TO_RESOURCE);
	}
}