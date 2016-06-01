package org.dsa.iot.jdbc.handlers;

import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.util.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteConnectionHandler implements Handler<ActionResult> {

    private static final Logger LOG = LoggerFactory
            .getLogger(DeleteConnectionHandler.class);

    private NodeManager manager;

    public DeleteConnectionHandler(NodeManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(ActionResult event) {
        LOG.debug("Entering delete connection handle");
        Node parent = manager.getSuperRoot();
        parent.removeChild(event.getNode().getParent());
        LOG.debug("Connection {} deleted", event.getNode().getParent().getName());
    }

}
