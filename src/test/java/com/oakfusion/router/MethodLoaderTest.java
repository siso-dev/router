package com.oakfusion.router;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodLoaderTest {

	private MethodLoader methodLoader = new MethodLoader();

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	@Test
	public void should_not_found_non_existing_method() throws NoSuchMethodException {
		// given
		thrown.expect(NoSuchMethodException.class);

		// when
		methodLoader.load(SampleController.class, "nonExistingMethod");
	}

	@Test
	public void should_find_handler_method() throws NoSuchMethodException {
		// when
		Method method = methodLoader.load(SampleController.class, "simpleCall");

		// then
		assertThat(method.getReturnType()).isEqualTo(SampleController.Pojo.class);
	}
}