package org.dsa.iot.jdbc.provider;

import java.util.Map;
import java.util.Map.Entry;
import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.node.Node;
import org.dsa.iot.dslink.node.NodeBuilder;
import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.json.JsonObject;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;

public class JdbcProvider extends ActionProvider {

    /**
     * Starts building Node's tree
     *
     * @param link link
     */
    public void run(DSLink link) {
        NodeManager manager = link.getNodeManager();
        Node superRoot = manager.getNode("/").getNode();

        Node status = superRoot.createChild(JdbcConstants.STATUS, false).build();
        status.setValueType(ValueType.STRING);
        status.setValue(new Value(JdbcConstants.READY));

        NodeBuilder builder = superRoot
                .createChild(JdbcConstants.ADD_CONNECTION_ACTION, false);
        builder.setAction(getAddConnectionAction(manager));
        builder.build();

        configureActions(superRoot, manager);
    }

    /**
     * Initial actions assignment
     *
     * @param superRoot root
     * @param manager   manager
     */
    private void configureActions(Node superRoot, NodeManager manager) {
        Map<String, Node> childs = superRoot.getChildren();

        for (Entry<String, Node> entry : childs.entrySet()) {
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
                String driver = object.get(JdbcConstants.DRIVER);
                config.setDriverName(driver);
                config.setNode(node);

                NodeBuilder builder = node
                        .createChild(JdbcConstants.DELETE_CONNECTION, false);
                builder.setAction(getDeleteConnectionAction(manager));
                builder.setSerializable(false);
                builder.build();

                builder = node.createChild(JdbcConstants.EDIT_CONNECTION, false);
                builder.setAction(getEditConnectionAction(config));
                builder.setSerializable(false);
                builder.build();

                {
                    builder = node.createChild(JdbcConstants.QUERY, false);
                    builder.setAction(getQueryAction(config));
                    builder.setSerializable(false);
                    builder.build();
                }
                {
                    builder = node.createChild(JdbcConstants.STREAMING_QUERY, false);
                    builder.setAction(getStreamingQueryAction(config));
                    builder.setSerializable(false);
                    builder.build();
                }

                if ("org.postgresql.Driver".equals(driver)) {
                    builder = node.createChild(JdbcConstants.COPY, false);
                    builder.setAction(getCopyAction(config));
                    builder.setSerializable(false);
                    builder.build();
                }

                {
                    builder = node.createChild(JdbcConstants.UPDATE, false);
                    builder.setAction(getUpdateAction(config));
                    builder.setSerializable(false);
                    builder.build();
                }

                Node status = node.createChild(JdbcConstants.STATUS, false).build();
                status.setValue(new Value(JdbcConstants.READY));
            }
        }
    }
}
