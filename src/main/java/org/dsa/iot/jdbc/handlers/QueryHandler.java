package org.dsa.iot.jdbc.handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.dsa.iot.dslink.methods.StreamState;
import org.dsa.iot.dslink.node.actions.ActionResult;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

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

		if (value != null && value.getString() != null
				&& !value.getString().isEmpty()) {

			String sql = value.getString();
			LOG.info(sql.toString());

			try {
				doQuery(sql, event);
			} catch (SQLException e) {
				setStatusMessage(e.getMessage());
			} catch (ClassNotFoundException e) {
				setStatusMessage(e.getMessage());
			}
		} else {
			setStatusMessage("slq is empty");
		}
	}

	private void doQuery(String query, ActionResult event) throws SQLException,
			ClassNotFoundException {
		Connection connection = getConnection();
		Statement stmt = null;
		ResultSet rSet = null;

		try {

			LOG.info("start quering");
			stmt = connection.createStatement();

			rSet = stmt.executeQuery(query);

			ResultSetMetaData meta = rSet.getMetaData();
			int columnCount = meta.getColumnCount();

			JsonArray columns = new JsonArray();
			JsonObject object = null;
			for (int i = 1; i <= columnCount; i++) {
				object = new JsonObject();
				object.putString("name", meta.getColumnName(i));
				object.putString("type", ValueType.STRING.toJsonString());
				columns.add(object);
			}

			JsonArray updates = new JsonArray();
			JsonArray value = null;
			while (rSet.next()) {
				value = new JsonArray();
				for (int i = 1; i <= columnCount; i++) {
					value.add(rSet.getString(i));
				}
				updates.addArray(value);
			}
			event.setColumns(columns);
			event.setUpdates(updates);

			StringBuilder builder = new StringBuilder();

			builder.append("success: ").append("number of rows returned: ")
					.append(updates.size());

			setStatusMessage(builder.toString());
			event.setStreamState(StreamState.CLOSED);
			LOG.info("send data");
		} catch (SQLException e) {
			setStatusMessage(e.getMessage());
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
					LOG.info("rSet.close()");
				}
			} catch (SQLException e) {
				setStatusMessage(e.getMessage());
			}
			try {
				if (stmt != null) {
					stmt.close();
					LOG.info("stmt.close()");
				}
			} catch (SQLException e) {
				setStatusMessage(e.getMessage());
			}
			try {
				if (connection != null) {
					connection.close();
					LOG.info("connection.close()");
				}
			} catch (SQLException e) {
				setStatusMessage(e.getMessage());
			}
		}
	}

	private Connection getConnection() throws SQLException {
		Connection connection = null;
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
				LOG.info(e.getMessage());
			}

			int first = config.getDriverName().indexOf(".");
			int next = config.getDriverName().indexOf(".", first + 1);

			String jdbcDriver = config.getDriverName().substring(first + 1,
					next);

			StringBuilder builder = new StringBuilder();
			builder.append("jdbc:").append(jdbcDriver).append("://")
					.append(config.getUrl());
			connection = DriverManager.getConnection(builder.toString(),
					config.getUser(), String.valueOf(config.getPassword()));
		}
		return connection;
	}

	private void setStatusMessage(String message) {
		LOG.info(message);
		config.getNode().getChild(JdbcConstants.STATUS)
				.setValue(new Value(message));
	}
}
