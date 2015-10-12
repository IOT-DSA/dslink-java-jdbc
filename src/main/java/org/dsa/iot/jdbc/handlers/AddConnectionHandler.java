package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.dslink.util.json.JsonObject;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.dsa.iot.jdbc.provider.ActionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddConnectionHandler extends ActionProvider implements
		Handler<ActionResult> {

	private static final Logger LOG = LoggerFactory
			.getLogger(AddConnectionHandler.class);

	private NodeManager manager;

	public AddConnectionHandler(NodeManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(ActionResult event) {
		LOG.info("Entering add connection handle");

		Value name = event.getParameter(JdbcConstants.NAME, new Value(""));
		Node child = manager.getSuperRoot().getChild(name.getString());
		Node status = manager.getSuperRoot().getChild(JdbcConstants.STATUS);
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

		Value driver = event.getParameter(JdbcConstants.DRIVER, new Value(""));
		if (driver.getString() == null || driver.getString().isEmpty()) {
			status.setValue(new Value("driver is empty"));
			return;
		}

		Value timeout = event.getParameter(JdbcConstants.DEFAULT_TIMEOUT,
				new Value(60));
		Value poolable = event.getParameter(JdbcConstants.POOLABLE);

		JdbcConfig config = new JdbcConfig();
		config.setName(name.getString());
		config.setUrl(url.getString());
		config.setUser(user.getString());
		config.setPassword(password.getString().toCharArray());
		config.setPoolable(poolable.getBool());
		config.setTimeout((Integer) timeout.getNumber());
		config.setDriverName(driver.getString());
		LOG.info(config.toString());

		// create DataSource if specified
		if (poolable.getBool()) {
			config.setDataSource(JdbcConnectionHelper
					.configureDataSource(config));
		}

		JsonObject object = new JsonObject();
		object.put(JdbcConstants.NAME, config.getName());
		object.put(JdbcConstants.URL, config.getUrl());
		object.put(JdbcConstants.USER, config.getUser());
		object.put(JdbcConstants.POOLABLE, config.isPoolable());
		object.put(JdbcConstants.DEFAULT_TIMEOUT, config.getTimeout());
		object.put(JdbcConstants.DRIVER, config.getDriverName());

		NodeBuilder builder = manager.createRootNode(name.getString());
		builder.setAttribute(JdbcConstants.ACTION, new Value(true));
		builder.setAttribute(JdbcConstants.CONFIGURATION, new Value(object));
		builder.setPassword(password.getString().toCharArray());
		Node conn = builder.build();
		config.setNode(conn);

		Node connStatus = conn.createChild(JdbcConstants.STATUS).build();
		connStatus.setValueType(ValueType.STRING);
		connStatus.setValue(new Value(JdbcConstants.CREATED));

		builder = conn.createChild(JdbcConstants.DELETE_CONNECTION);
		builder.setAction(getDeleteConnectionAction(manager));
		builder.build();

		builder = conn.createChild(JdbcConstants.EDIT_CONNECTION);
        builder.setAction(getEditConnectionAction(config));
        builder.build();
		LOG.info("Connection {} created", conn.getName());

		builder = conn.createChild(JdbcConstants.QUERY);
		builder.setAction(getQueryAction(config));
		builder.build();

        builder = conn.createChild(JdbcConstants.UPDATE);
        builder.setAction(getUpdateAction(config));
        builder.build();

		status.setValue(new Value("connection created"));
	}
}
