package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.dsa.iot.jdbc.provider.ActionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

public class EditConnectionAction extends ActionProvider implements
		Handler<ActionResult> {

	private static final Logger LOG = LoggerFactory
			.getLogger(EditConnectionAction.class);

	private JdbcConfig config;

	public EditConnectionAction(JdbcConfig config) {
		this.config = config;
	}

	@Override
	public void handle(ActionResult event) {
		LOG.info("Entering edit connection handle");

		Node status = config.getNode().getChild(JdbcConstants.STATUS);
		Value name = event.getParameter(JdbcConstants.NAME, new Value(""));

		Node child = config.getNode().getParent().getChild(name.getString());
		if (name.getString() != null && !name.getString().isEmpty()) {
			if (child != null) {
				status.setValue(new Value("connection with name "
						+ name.getString() + " alredy exist"));
				return;
			}
		} else {
			status.setValue(new Value("name is empty"));
			return;
		}

		Value url = event.getParameter(JdbcConstants.URL, new Value(""));
		if (url.getString() == null || url.getString().isEmpty()) {
			status.setValue(new Value("url is empty"));
			return;
		}

		Value user = event.getParameter(JdbcConstants.USER, new Value(""));
		Value password = event.getParameter(JdbcConstants.PASSWORD, new Value(
				""));

		LOG.info("Old configuration is {}", config);
		config.setName(name.getString());
		config.setUrl(url.getString());
		config.setUser(user.getString());
		config.setPassword(password.getString().toCharArray());
		LOG.info("New configuration is {}", config);

		JsonObject object = new JsonObject();
		object.putString(JdbcConstants.NAME, config.getName());
		object.putString(JdbcConstants.URL, config.getUrl());
		object.putString(JdbcConstants.USER, config.getUser());

		Node edit = event.getNode();
		edit.setAction(getEditConnectioAction(config));

		Node connection = config.getNode();
		connection.setDisplayName(name.getString());
		connection.setAttribute(JdbcConstants.CONFIGURATION, new Value(object));
		connection.setPassword(config.getPassword());
	}
}
