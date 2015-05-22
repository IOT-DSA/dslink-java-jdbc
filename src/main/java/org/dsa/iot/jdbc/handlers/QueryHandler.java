package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;

public class QueryHandler implements Handler<ActionResult> {

	private static final Logger LOG = LoggerFactory
			.getLogger(QueryHandler.class);

	private JdbcConfig config;

	public QueryHandler(JdbcConfig config) {
		this.config = config;
	}

	@Override
	public void handle(ActionResult event) {
		LOG.info("Entering query connection handle");

	}
}
