package com.oakfusion.router.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UriTreeTest {

	public static final String DATA = "data";
	public static final String CHILD_KEY = "child";

	private final UriTree<String> tree = new UriTree<String>("root");

	@Test
	public void should_be_an_empty_leaf_after_creation() {
		assertThat(tree.hasData()).isFalse();
		assertThat(tree.isLeaf()).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_add_empty_leaf_by_single_element_path() {
		// when
		UriTree<String> node = tree.put(CHILD_KEY);

		// then
		assertThat(node).isNotEqualTo(tree);
		assertThat(node.hasData()).isFalse();
		assertThat(node.isLeaf()).isTrue();
		assertThat(node.getKey()).isEqualTo(CHILD_KEY);
	}

	@Test
	public void should_add_data_leaf_by_single_element_path() {
		// when
		UriTree<String> node = tree.put(CHILD_KEY, DATA);

		// then
		assertThat(node).isNotEqualTo(tree);
		assertThat(node.hasData()).isTrue();
		assertThat(node.getData()).isEqualTo(DATA);
		assertThat(node.isLeaf()).isTrue();
		assertThat(node.getKey()).isEqualTo(CHILD_KEY);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_add_empty_leaf_by_single_element_array_path() {
		// given
		String[] path = new String[] {CHILD_KEY};

		// when
		UriTree<String> node = tree.put(path);

		// then
		assertThat(node.hasData()).isFalse();
		assertThat(node.isLeaf()).isTrue();
		assertThat(node.getKey()).isEqualTo(CHILD_KEY);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_add_data_leaf_by_single_element_array_path() {
		// given
		String[] path = new String[] {CHILD_KEY};

		// when
		UriTree<String> node = tree.put(path, DATA);

		// then
		assertThat(node.hasData()).isTrue();
		assertThat(node.isLeaf()).isTrue();
		assertThat(node.getKey()).isEqualTo(CHILD_KEY);
		assertThat(node.getData()).isEqualTo(DATA);
	}

	@Test
	public void should_add_data_by_path() {
		// given
		String path = "s1/s2/s3";

		// when
		UriTree<String> node = tree.put(path, DATA);

		// then
		assertThat(node.hasData()).isTrue();
		assertThat(node.getKey()).isEqualTo("s3");
		assertThat(node.getData()).isEqualTo(DATA);
	}

	@Test
	public void should_add_data_by_absolute_path() {
		// given
		String path = "/s1/s2/s3";

		// when
		UriTree<String> node = tree.put(path, DATA);

		// then
		assertThat(node.hasData()).isTrue();
		assertThat(node.getKey()).isEqualTo("s3");
		assertThat(node.getData()).isEqualTo(DATA);
	}

	@Test
	public void should_ignore_ending_separator_while_adding_data() {
		// given
		String path = "/s1/s2/s3/";

		// when
		UriTree<String> node = tree.put(path, DATA);

		// then
		assertThat(node.hasData()).isTrue();
		assertThat(node.getKey()).isEqualTo("s3");
		assertThat(node.getData()).isEqualTo(DATA);
	}

	@Test
	public void should_ignore_empty_path_segments_while_adding_data() {
		// given
		String path = "/s1//s2//";

		// when
		UriTree<String> node = tree.put(path, DATA);

		// then
		assertThat(node.hasData()).isTrue();
		assertThat(node.getKey()).isEqualTo("s2");
		assertThat(node.getData()).isEqualTo(DATA);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_for_empty_path_array() {
		// when
		tree.put(new String[]{});
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_for_path_array_of_empty_strings() {
		// when
		tree.put(new String[]{"", ""});
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_for_empty_path() {
		// when
		tree.put("");
	}

	@Test
	public void should_add_multiple_nodes_sharing_same_paths() {
		// when
		tree.put("s1/s2/s3", "1");
		tree.put("s1/s2/s4", "2");
		tree.put("s1/s2", "3");
		tree.put("/s1/", "4");
		tree.put("s2/s1", "x");
		tree.put("s2/s3/s4/s5");

		// then
		assertThat(tree.get("s1/s2").getData()).isEqualTo("3");
		assertThat(tree.get("s1/s2/s4").getData()).isEqualTo("2");
		assertThat(tree.get("s1/s2/s3").getData()).isEqualTo("1");
		assertThat(tree.get("s1").getData()).isEqualTo("4");
		assertThat(tree.get("s2/s1").getData()).isEqualTo("x");
		assertThat(tree.get("s2/s3/s4/s5").getData()).isNull();
	}

	@Test
	public void should_not_find_anything_for_leaf() {
		// when
		final UriTree<String> found = tree.get(new String[]{CHILD_KEY});

		// then
		assertThat(found).isNull();
	}

	@Test
	public void should_not_find_anything_for_empty_path_element_array() {
		// given
		final String[] path = new String[]{"s1"};
		tree.put(path);

		// when
		final UriTree<String> found = tree.get(new String[]{});

		// then
		assertThat(found).isNull();
	}

	@Test
	public void should_not_find_anything_for_path_array_of_empty_element() {
		// given
		final String[] path = new String[]{"s1"};
		tree.put(path);

		// when
		final UriTree<String> found = tree.get(new String[]{""});

		// then
		assertThat(found).isNull();
	}

	@Test
	public void should_find_node_by_single_element_path_array() {
		// given
		final String[] path = new String[]{"s1"};
		final UriTree created = tree.put(path);

		// when
		UriTree<String> found = tree.get(path);

		// then
		assertThat(found).isSameAs(created);
	}

	@Test
	public void should_find_by_node_by_path_array() {
		// given
		final String[] path = new String[]{"s1", "s2", "s3"};
		final UriTree created = tree.put(path);

		// when
		UriTree<String> found = tree.get(path);

		// then
		assertThat(found).isSameAs(created);
	}

	@Test
	public void should_find_node_by_string_path() {
		// given
		final String path = "s1/s2/s3";
		final UriTree created = tree.put(path);

		// when
		UriTree<String> found = tree.get(path);

		// then
		assertThat(found).isSameAs(created);
	}

	@Test
	public void should_find_deepest_matching_data_node_by_path_array() {
		// given
		tree.put("s1/s2/s3", "1");
		tree.put("s1/s2/s4", "2");
		tree.put("s1/s2", "3");
		tree.put("/s1/", "4");
		tree.put("s2/s1", "x");
		tree.put("s2/s3/s4/s5");
		final String[] path = {"s1", "s2", "s3"};

		// when
		final UriTree<String> matched = tree.matchedBySegments(path);

		// then
		assertThat(matched.getData()).isEqualTo("1");
	}

	@Test
	public void should_find_deepest_matching_data_node_by_path() {
		// given
		tree.put("s1/s2/s3", "1");
		tree.put("s1/s2/s4", "2");
		tree.put("s1/s2", "3");
		tree.put("/s1/", "4");
		tree.put("s2/s1", "x");
		tree.put("s2/s3/s4/s5");
		final String path = "s1/s2/s3";

		// when
		final UriTree<String> matched = tree.matchedBySegments(path);

		// then
		assertThat(matched.getData()).isEqualTo("1");
	}

	@Test
	public void should_build_parameterized_node() {
		// given
		tree.put("s1/s2/{param}", "1");
		final String path = "s1/s2/xxx";

		// when
		final UriTree<String> matched = tree.matchedBySegments(path);

		// then
		assertThat(matched.getData()).isEqualTo("1");
	}

}
