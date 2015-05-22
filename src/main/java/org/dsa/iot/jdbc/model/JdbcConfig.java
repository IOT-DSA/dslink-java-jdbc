package org.dsa.iot.jdbc.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.dsa.iot.dslink.node.Node;

public class JdbcConfig {
	private String name;
	private String url;
	private String user;
	private char[] password;
	private Node node;
	private boolean poolable;
	private Integer timeout;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public char[] getPassword() {
		return password == null ? null : password.clone();
	}

	public void setPassword(char[] password) {
		this.password = password == null ? null : password.clone();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("name", name).append("user", user).append("url", url)
				.append("pool", poolable).append("timeout", timeout).toString();
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public boolean isPoolable() {
		return poolable;
	}

	public void setPoolable(boolean pool) {
		this.poolable = pool;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
}
