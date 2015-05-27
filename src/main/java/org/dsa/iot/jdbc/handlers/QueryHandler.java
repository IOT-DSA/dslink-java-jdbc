package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
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

		Value value = event.getParameter(JdbcConstants.SQL);

		Node status = config.getNode().getChild(JdbcConstants.STATUS);
		if (value != null && value.getString() != null
				&& !value.getString().isEmpty()) {

			String sql = value.getString();
			LOG.info(sql.toString());
		} else {
			status.setValue(new Value("slq is empty"));
		}
	}
}
