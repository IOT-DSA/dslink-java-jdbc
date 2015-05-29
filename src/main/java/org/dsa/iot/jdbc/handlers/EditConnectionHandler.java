package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.dsa.iot.jdbc.provider.ActionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

public class EditConnectionHandler extends ActionProvider implements
		Handler<ActionResult> {

	private static final Logger LOG = LoggerFactory
			.getLogger(EditConnectionHandler.class);

	private JdbcConfig config;

	public EditConnectionHandler(JdbcConfig config) {
		this.config = config;
	}

	@Override
	public void handle(ActionResult event) {
		LOG.info("Entering edit connection handle");

		Node status = config.getNode().getChild(JdbcConstants.STATUS);

		Value url = event.getParameter(JdbcConstants.URL, new Value(""));
		if (url.getString() == null || url.getString().isEmpty()) {
			status.setValue(new Value("url is empty"));
			return;
		}

		Value user = event.getParameter(JdbcConstants.USER, new Value(""));
		Value password = event.getParameter(JdbcConstants.PASSWORD, new Value(
				""));

		Value driver = event.getParameter(JdbcConstants.DRIVER, new Value(""));
		if (driver.getString() == null || driver.getString().isEmpty()) {
			status.setValue(new Value("driver is empty"));
			return;
		}

		LOG.info("Old configuration is {}", config);
		config.setUrl(url.getString());
		config.setUser(user.getString());
		config.setPassword(password.getString().toCharArray());
		config.setDriverName(driver.getString());
		config.setDataSource(JdbcConnectionHelper.configureDataSource(config));
		LOG.info("New configuration is {}", config);

		Node edit = event.getNode();
		edit.setAction(getEditConnectioAction(config));

		Node connection = config.getNode();

		JsonObject object = connection
				.getAttribute(JdbcConstants.CONFIGURATION).getMap();
		object.putString(JdbcConstants.NAME, config.getName());
		object.putString(JdbcConstants.URL, config.getUrl());
		object.putString(JdbcConstants.USER, config.getUser());
		object.putString(JdbcConstants.DRIVER, config.getDriverName());
		connection.setAttribute(JdbcConstants.CONFIGURATION, new Value(object));
		connection.setPassword(config.getPassword());
	}
}
