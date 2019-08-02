package org.dsa.iot.jdbc.handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryHandler implements Handler<ActionResult> {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueryHandler.class);

    private JdbcConfig config;

    public QueryHandler(JdbcConfig config) {
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
                doQuery(sql, event);
            } catch (SQLException | ClassNotFoundException e) {
                setStatusMessage(e.getMessage(), e);
            }
        } else {
            setStatusMessage("sql is empty", null);
        }
    }

    private void doQuery(String query, ActionResult event) throws SQLException,
            ClassNotFoundException {
        Connection connection = getConnection();
        Statement stmt = null;
        ResultSet rSet = null;

        try {

            LOG.debug("start querying");
            stmt = connection.createStatement();

            rSet = stmt.executeQuery(query);

            ResultSetMetaData meta = rSet.getMetaData();
            int columnCount = meta.getColumnCount();

            Table table = event.getTable();
            for (int i = 1; i <= columnCount; i++) {
                ValueType type = ValueType.STRING;
                Parameter p = new Parameter(meta.getColumnName(i), type);
                table.addColumn(p);
            }

            int size = 0;
            while (rSet.next()) {
                Row row = new Row();
                for (int i = 1; i <= columnCount; i++) {
                    row.addValue(new Value(rSet.getString(i)));
                }
                table.addRow(row);
                size++;
            }

            String builder = "success: number of rows returned: " + size;
            setStatusMessage(builder, null);
            event.setStreamState(StreamState.CLOSED);
            LOG.debug("send data");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("No results were returned by the query")) {
                LOG.warn("No results were returned by the query. For queries that shouldn't return results, use the Update action instead");
            }
            setStatusMessage(msg, e);
        } finally {
            try {
                if (rSet != null) {
                    rSet.close();
                    LOG.debug("rSet.close()");
                }
            } catch (SQLException e) {
                setStatusMessage(e.getMessage(), e);
            }
            try {
                if (stmt != null) {
                    stmt.close();
                    LOG.debug("stmt.close()");
                }
            } catch (SQLException e) {
                setStatusMessage(e.getMessage(), e);
            }
            try {
                if (connection != null) {
                    connection.close();
                    LOG.debug("connection.close()");
                }
            } catch (SQLException e) {
                setStatusMessage(e.getMessage(), e);
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

    private void setStatusMessage(String message, Exception e) {
        if (e == null) {
            LOG.debug(message);
        } else {
            LOG.warn(message, e);
        }
        config.getNode().getChild(JdbcConstants.STATUS, false)
              .setValue(new Value(message));
    }
}
