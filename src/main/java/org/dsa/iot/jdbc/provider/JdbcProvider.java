package org.dsa.iot.jdbc.provider;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.json.JsonObject;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class JdbcProvider extends ActionProvider {

	/**
	 * Starts building Node's tree
	 * 
	 * @param link
	 */
	public void run(DSLink link) {
		NodeManager manager = link.getNodeManager();
		Node superRoot = manager.getNode("/").getNode();

		Node status = superRoot.createChild(JdbcConstants.STATUS).build();
		status.setValueType(ValueType.STRING);
		status.setValue(new Value(JdbcConstants.READY));

		NodeBuilder builder = superRoot
				.createChild(JdbcConstants.ADD_CONNECTION_ACTION);
		builder.setAction(getAddConnectionAction(manager));
		builder.build();

		configureActions(superRoot, manager);
	}

	/**
	 * Initial actions assignment
	 * 
	 * @param superRoot
	 * @param manager
	 */
	private void configureActions(Node superRoot, NodeManager manager) {
		Map<String, Node> childs = superRoot.getChildren();

		for (Iterator<Entry<String, Node>> iterator = childs.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, Node> entry = iterator.next();
			Node node = entry.getValue();
			if (node.getAttribute(JdbcConstants.ACTION) != null
					&& node.getAttribute(JdbcConstants.ACTION).getBool()) {

				JsonObject object = node.getAttribute(
						JdbcConstants.CONFIGURATION).getMap();
				JdbcConfig config = new JdbcConfig();
				config.setName((String) object.get(JdbcConstants.NAME));
				config.setUrl((String) object.get(JdbcConstants.URL));
				config.setUser((String) object.get(JdbcConstants.USER));
				config.setPassword(node.getPassword());
				config.setPoolable((Boolean) object.get(JdbcConstants.POOLABLE));
				config.setTimeout((Integer) object.get(JdbcConstants.DEFAULT_TIMEOUT));
				config.setDriverName((String) object.get(JdbcConstants.DRIVER));
				config.setNode(node);

				NodeBuilder builder = node
						.createChild(JdbcConstants.DELETE_CONNECTION);
				builder.setAction(getDeleteConnectionAction(manager));
				builder.build();

				builder = node.createChild(JdbcConstants.EDIT_CONNECTION);
				builder.setAction(getEditConnectionAction(config));
				builder.build();

				builder = node.createChild(JdbcConstants.QUERY);
				builder.setAction(getQueryAction(config));
				builder.build();

                builder = node.createChild(JdbcConstants.UPDATE);
                builder.setAction(getUpdateAction(config));
                builder.build();

				Node status = node.createChild(JdbcConstants.STATUS).build();
				status.setValue(new Value(JdbcConstants.READY));
			}
		}
	}
}
