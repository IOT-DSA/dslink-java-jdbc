package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.dslink.util.json.JsonObject;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.dsa.iot.jdbc.provider.ActionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		Value timeout = event.getParameter(JdbcConstants.DEFAULT_TIMEOUT,
				new Value(60));
		Value poolable = event.getParameter(JdbcConstants.POOLABLE);

		LOG.info("Old configuration is {}", config);
		config.setUrl(url.getString());
		config.setUser(user.getString());
		config.setPassword(password.getString().toCharArray());
		config.setDriverName(driver.getString());
		config.setPoolable(poolable.getBool());

		// create DataSource if specified
		config.setTimeout((Integer) timeout.getNumber());
		if (poolable.getBool()) {
			config.setDataSource(JdbcConnectionHelper
					.configureDataSource(config));
		} else {
			config.setDataSource(null);
		}

		LOG.info("New configuration is {}", config);

		Node edit = event.getNode();
		edit.setAction(getEditConnectionAction(config));

		Node connection = config.getNode();

		JsonObject object = connection
				.getAttribute(JdbcConstants.CONFIGURATION).getMap();
		object.put(JdbcConstants.NAME, config.getName());
		object.put(JdbcConstants.URL, config.getUrl());
		object.put(JdbcConstants.USER, config.getUser());
		object.put(JdbcConstants.DRIVER, config.getDriverName());
		object.put(JdbcConstants.POOLABLE, config.isPoolable());
		object.put(JdbcConstants.DEFAULT_TIMEOUT, config.getTimeout());
		connection.setAttribute(JdbcConstants.CONFIGURATION, new Value(object));
		connection.setPassword(config.getPassword());
	}
}
