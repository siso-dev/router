package com.oakfusion.router;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class RouterTest {

	public static final String URI_TO_RESOURCE = "/uri/to/resource";
	public static final String CUSTOM_HTTP_METHOD = "CUSTOM_HTTP_METHOD";
	public static final String METHOD_NAME = "simpleCall";
	public static final String NON_EXISTING_METHOD_NAME = "nem";

	private Router router = new Router();

	@Rule
	public ExpectedException thrown= ExpectedException.none();

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
		assertThat(route.getHttpMethod()).isEqualTo("GET");
		assertThat(route.getUri()).isEqualTo(URI_TO_RESOURCE);
	}

	@Test
	public void should_throw_when_invalid_handler_method_specified() {
		// given
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(NON_EXISTING_METHOD_NAME);

		// when
		router.route(URI_TO_RESOURCE).whenGET().handleIn(SampleController.class).by(NON_EXISTING_METHOD_NAME);
	}

	@Test
	public void should_build_next_route_to_the_same_resource_after_competing_first_one() {
		// when
		router.route(URI_TO_RESOURCE)
				.whenGET().handleIn(SampleController.class).by(METHOD_NAME)
				.whenPUT().handleIn(SampleController.class).by(METHOD_NAME);

		// then
		Route route = router.getRouteFor("GET", URI_TO_RESOURCE);
		assertThat(route.getHttpMethod()).isEqualTo("GET");
		assertThat(route.getUri()).isEqualTo(URI_TO_RESOURCE);
		route = router.getRouteFor("PUT", URI_TO_RESOURCE);
		assertThat(route.getHttpMethod()).isEqualTo("PUT");
		assertThat(route.getUri()).isEqualTo(URI_TO_RESOURCE);
	}

	@Test
	public void should_build_next_route_to_different_resource_after_competing_first_one() {
		// when
		router
			.route("/u1").whenGET().handleIn(SampleController.class).by(METHOD_NAME)
			.route("/u2").whenPUT().handleIn(SampleController.class).by(METHOD_NAME);

		// then
		Route route = router.getRouteFor("GET", "/u1");
		assertThat(route.getHttpMethod()).isEqualTo("GET");
		assertThat(route.getUri()).isEqualTo("/u1");
		route = router.getRouteFor("PUT", "/u2");
		assertThat(route.getHttpMethod()).isEqualTo("PUT");
		assertThat(route.getUri()).isEqualTo("/u2");
	}

	@Test
	public void should_not_allow_to_invoke_http_method_builder_before_route_specification() {
		// given
		thrown.expect(RuntimeException.class);
		thrown.expectMessage("Route context not initialized");

		// when
		router.whenGET();
	}

}
