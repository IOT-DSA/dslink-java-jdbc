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

public class ConfigureConnectionAction extends ActionProvider implements
		Handler<ActionResult> {

	private static final Logger LOG = LoggerFactory
			.getLogger(ConfigureConnectionAction.class);

	private JdbcConfig config;

	public ConfigureConnectionAction(JdbcConfig config) {
		this.config = config;
	}

	@Override
	public void handle(ActionResult event) {
		LOG.info("Entering configure connection handle");
		LOG.info("Old configuration is {}", config);

		Value timeout = event.getParameter(JdbcConstants.TIMEOUT);
		Value poolable = event.getParameter(JdbcConstants.POOLABLE);

		config.setPoolable(poolable.getBool());
		config.setTimeout((Integer) timeout.getNumber());

		Node connection = config.getNode();

		JsonObject configuration = connection.getAttribute(
				JdbcConstants.CONFIGURATION).getMap();
		configuration.putBoolean(JdbcConstants.POOLABLE, config.isPoolable());
		configuration.putNumber(JdbcConstants.TIMEOUT, config.getTimeout());

		connection.setAttribute(JdbcConstants.CONFIGURATION, new Value(
				configuration));

		event.getNode().setAction(getConfigureConnectioAction(config));

		LOG.info("New configuration is {}", config);
	}
}
