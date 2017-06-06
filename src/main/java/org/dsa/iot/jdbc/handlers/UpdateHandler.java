package org.dsa.iot.jdbc.handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Samuel Grenier
 */
public class UpdateHandler implements Handler<ActionResult> {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueryHandler.class);

    private JdbcConfig config;

    public UpdateHandler(JdbcConfig config) {
        this.config = config;
    }

    @Override
    public void handle(ActionResult event) {
        LOG.debug("Entering query connection handle");

        Value value = event.getParameter(JdbcConstants.SQL);

        if (value != null && value.getString() != null
                && !value.getString().isEmpty()) {

            String sql = value.getString();
            LOG.debug(sql);

            try {
                doUpdate(sql, event);
            } catch (SQLException | ClassNotFoundException e) {
                setStatusMessage(e.getMessage());
            }
        } else {
            setStatusMessage("sql is empty");
        }
    }

    private void doUpdate(String query, ActionResult event)
            throws SQLException, ClassNotFoundException {
        Connection connection = getConnection();
        Statement stmt = null;
        try {
            LOG.debug("start update");
            stmt = connection.createStatement();

            int updates = stmt.executeUpdate(query);

            Table table = event.getTable();
            table.addRow(Row.make(new Value(updates)));

            String builder = "success: number of rows updated: " + updates;
            setStatusMessage(builder);
            event.setStreamState(StreamState.CLOSED);
            LOG.debug("send data");
        } catch (SQLException e) {
            setStatusMessage(e.getMessage());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    LOG.debug("stmt.close()");
                }
            } catch (SQLException e) {
                setStatusMessage(e.getMessage());
            }
            try {
                if (connection != null) {
                    connection.close();
                    LOG.debug("connection.close()");
                }
            } catch (SQLException e) {
                setStatusMessage(e.getMessage());
            }
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection;
        if (config.isPoolable()) {
            if (config.getDataSource() == null) {
                config.setDataSource(JdbcConnectionHelper
                                             .configureDataSource(config));
            }
            connection = config.getDataSource().getConnection();
        } else {
            try {
                Class.forName(config.getDriverName());
            } catch (ClassNotFoundException e) {
                LOG.debug(e.getMessage());
            }

            connection = DriverManager.getConnection(config.getUrl(),
                                                     config.getUser(),
                                                     String.valueOf(config.getPassword()));
        }
        return connection;
    }

    private void setStatusMessage(String message) {
        LOG.debug(message);
        config.getNode().getChild(JdbcConstants.STATUS, false)
              .setValue(new Value(message));
    }
}
