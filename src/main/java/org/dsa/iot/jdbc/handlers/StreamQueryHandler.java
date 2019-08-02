package org.dsa.iot.jdbc.handlers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import org.dsa.iot.commons.Container;
import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.table.Row;
import org.dsa.iot.dslink.node.actions.table.Table;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.dslink.util.Objects;
import org.dsa.iot.dslink.util.handler.Handler;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamQueryHandler implements Handler<ActionResult> {

    private static final Logger LOG = LoggerFactory
            .getLogger(StreamQueryHandler.class);

    private static final char[] ALPHA_CHARS;
    private static final Random RANDOM = new Random();

    private JdbcConfig config;

    public StreamQueryHandler(JdbcConfig config) {
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

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE")
    private void doQuery(final String query, ActionResult event) throws SQLException,
            ClassNotFoundException {
        final Connection connection = getConnection();
        final Container<ResultSet> rSet = new Container<>();
        final Statement stmt;

        try {
            final String cursName = randomCursorName();
            connection.setAutoCommit(false);
            stmt = connection.createStatement();

            LOG.debug("start querying");
            stmt.execute("DECLARE " + cursName + " CURSOR FOR " + query);
            rSet.setValue(stmt.executeQuery("FETCH NEXT FROM " + cursName));

            ResultSetMetaData meta = rSet.getValue().getMetaData();
            final int columnCount = meta.getColumnCount();

            final Table table = event.getTable();
            for (int i = 1; i <= columnCount; i++) {
                ValueType type = ValueType.STRING;
                Parameter p = new Parameter(meta.getColumnName(i), type);
                table.addColumn(p);
            }

            while (rSet.getValue().next()) {
                Row row = new Row();
                for (int i = 1; i <= columnCount; i++) {
                    row.addValue(new Value(rSet.getValue().getString(i)));
                }
                table.addRow(row);
            }
            try {
                rSet.getValue().close();
            } catch (SQLException e) {
                LOG.error("", e);
            }

            table.setMode(Table.Mode.STREAM);
            event.setStreamState(StreamState.INITIALIZED);

            final Container<Boolean> running = new Container<>(true);
            event.setCloseHandler(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                    running.setValue(false);
                }
            });

            Objects.getDaemonThreadPool().execute(new Runnable() {

                boolean sentReady = false;

                @Override
                public void run() {
                    try {
                        while (running.getValue()) {
                            rSet.setValue(stmt.executeQuery("FETCH NEXT FROM " + cursName));
                            int size = 0;
                            while (running.getValue() && rSet.getValue().next()) {
                                size++;
                                Row row = new Row();
                                for (int i = 1; i <= columnCount; i++) {
                                    row.addValue(new Value(rSet.getValue().getString(i)));
                                }
                                if (!sentReady) {
                                    table.sendReady();
                                    sentReady = true;
                                }
                                table.addRow(row);
                            }
                            rSet.getValue().close();
                            if (size == 0) {
                                table.close();
                            }
                        }
                    } catch (SQLException e) {
                        LOG.error("", e);
                        table.close();
                    } finally {
                        try {
                            stmt.execute("CLOSE " + cursName);
                        } catch (SQLException ignored) {
                        }

                        try {
                            stmt.close();
                        } catch (SQLException ignored) {
                        }

                        try {
                            connection.close();
                        } catch (SQLException ignored) {
                        }
                    }
                }
            });
        } catch (SQLException e) {
            setStatusMessage(e.getMessage(), e);
        } finally {
            if (rSet.getValue() != null) {
                try {
                    rSet.getValue().close();
                } catch (SQLException ignored) {
                }
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

    private static String randomCursorName() {
        char[] buf = new char[8];
        for (int i = 0; i < buf.length; ++i) {
            buf[i] = ALPHA_CHARS[RANDOM.nextInt(ALPHA_CHARS.length)];
        }
        return new String(buf);
    }

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            tmp.append(ch);
        }
        ALPHA_CHARS = tmp.toString().toCharArray();
    }
}
