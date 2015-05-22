package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;

public class DeleteConnectionAction implements Handler<ActionResult> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DeleteConnectionAction.class);

	private NodeManager manager;

	public DeleteConnectionAction(NodeManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(ActionResult event) {
		LOG.info("Entering delete connection handle");
		Node parent = manager.getSuperRoot();
		parent.removeChild(event.getNode().getParent());
		LOG.info("Connection {} deleted", event.getNode().getParent().getName());
	}

}
