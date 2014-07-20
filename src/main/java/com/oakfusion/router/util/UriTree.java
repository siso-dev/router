package com.oakfusion.router.util;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.split;

public class UriTree<V> {

	public static final String PATH_SEPARATOR = "/";

	private final String key;
	private String parameter;
	private Map<String, UriTree<V>> children = new HashMap<>();
	private V data;

	public UriTree(final String root) {
		this.key = root;
	}

	public UriTree(final String key, final V data) {
		this(key);
		this.data = data;
	}

	public boolean hasData() {
		return data != null;
	}

	public V getData() {
		return data;
	}

	private void setData(final V data) {
		this.data = data;
	}

	public String getKey() {
		return key;
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	@Override
	public String toString() {
		return format("UriTree %s: [%s], data: [%s], children: [%d]",
				isLeaf() ? "leaf" : "node", key, data, children.size());
	}

	public UriTree put(final String[] path) {
		return put(path, null);
	}

	@SuppressWarnings("unchecked")
	public UriTree<V> put(final String pathString, final V data) {
		final String[] path = split(pathString, PATH_SEPARATOR);
		return put(path, data);
	}

	public UriTree put(final String path) {
		return put(path, null);
	}

	public UriTree put(final String[] path, final V data) {
		if (path.length == 0) {
			throw new IllegalArgumentException("path cannot be empty");
		}
		return putRecursive(this, path, 0, data);
	}

	public UriTree<V> get(final String pathString) {
		final String[] path = split(pathString, PATH_SEPARATOR);
		return get(path);
	}

	public UriTree<V> get(final String[] path) {
		return getRecursive(this, path, 0);
	}

	private UriTree putRecursive(final UriTree<V> tree, final String[] path, final int idx, final V data) {
		final String currentKey = path[idx];
		if (isEmpty(currentKey)) {
			throw new IllegalArgumentException("path cannot contain empty elements");
		}
		if (isParameterName(currentKey)) {
			tree.parameter = currentKey;
		}
		final boolean isTerminalNode = path.length == idx + 1;
		UriTree<V> node = tree.children.get(currentKey);
		if (node == null) {
			node = new UriTree<>(currentKey, isTerminalNode ? data : null);
			tree.children.put(currentKey, node);
		}
		if (isTerminalNode) {
			node.setData(data);
		}
		return isTerminalNode ? node : putRecursive(node, path, idx + 1, data);
	}

	private boolean isParameterName(String currentKey) {
		return currentKey.startsWith("{") && currentKey.endsWith("}");
	}

	private UriTree<V> getRecursive(final UriTree<V> tree, final String[] path, final int idx) {
		if (escapeRecursion(tree, path, idx)) {
			return null;
		}
		final String currentKey = path[idx];
		final UriTree<V> currentChild = tree.children.get(currentKey);
		if (currentChild == null) {
			return null;
		}
		if (currentChild.isLeaf() || idx == path.length - 1) {
			return currentChild;
		}
		return getRecursive(currentChild, path, idx + 1);
	}

	private boolean escapeRecursion(final UriTree<V> tree, final String[] path, final int idx) {
		final int length = path.length;
		return tree.isLeaf() || length == 0 || idx >= length || isEmpty(path[idx]);
	}

	public UriTree<V> matchedBySegments(final String[] path) {
		UriTree<V> currentNode = this;
		for (String pathString : path) {
			UriTree<V> node = currentNode.get(pathString);
			if (node == null) {
				node = currentNode.getParameterized();
				if (node == null) {
					return null;
				}
			}
			if (node.hasData() && node.isLeaf()) {
				return node;
			}
			currentNode = node;
		}
		return null;
	}

	private UriTree<V> getParameterized() {
		return get(parameter);
	}

	public UriTree<V> matchedBySegments(final String pathString) {
		final String[] path = split(pathString, PATH_SEPARATOR);
		return matchedBySegments(path);
	}

	public boolean matchesBySegments(final String[] path) {
		return matchedBySegments(path) != null;
	}

	public boolean matchesBySegments(final String pathString) {
		final String[] path = split(pathString, PATH_SEPARATOR);
		return matchesBySegments(path);
	}

}
