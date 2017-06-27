package org.dsa.iot.jdbc.postgres;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp2.DelegatingConnection;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.handlers.QueryHandler;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Samuel Grenier
 */
public class PostgresCopyHandler implements Handler<ActionResult> {

    private static final Logger LOG = LoggerFactory
            .getLogger(QueryHandler.class);

    private JdbcConfig config;

    public PostgresCopyHandler(JdbcConfig config) {
        this.config = config;
    }

    @Override
    public void handle(ActionResult event) {
        LOG.debug("Entering query connection handle");

        Value vSql = event.getParameter(JdbcConstants.SQL);
        if (vSql == null || vSql.getString() == null
                || vSql.getString().isEmpty()) {
            setStatusMessage("sql is empty");
            return;
        }

        Value vRow = event.getParameter(JdbcConstants.ROWS);
        if (vRow == null || vRow.getString() == null
                || vRow.getString().isEmpty()) {
            setStatusMessage("row is empty");
            return;
        }

        String sql = vSql.getString();
        String row = vRow.getString();
        LOG.debug("Running copy: {} => {}", sql, row);

        try {
            doUpdate(sql, row, event);
        } catch (SQLException | ClassNotFoundException | IOException e) {
            setStatusMessage(e.getMessage());
        }
    }

    private void doUpdate(String query, String row, ActionResult event)
            throws SQLException, ClassNotFoundException, IOException {
        Connection connection = getConnection();
        try {
            LOG.debug("start copy");
            connection.setAutoCommit(false);

            long updates;
            {
                CopyManager pcm;
                if (connection instanceof DelegatingConnection) {
                    DelegatingConnection c = (DelegatingConnection) connection;
                    pcm = ((PGConnection) c.getInnermostDelegateInternal()).getCopyAPI();
                } else {
                    pcm = ((PGConnection) connection).getCopyAPI();
                }
                StringReader read = new StringReader(row);
                updates = pcm.copyIn(query, read);
            }

            Table table = event.getTable();
            table.addRow(Row.make(new Value(updates)));

            String builder = "success: number of rows updated: " + updates;
            setStatusMessage(builder);
            event.setStreamState(StreamState.CLOSED);
            LOG.debug("send data");
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
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
