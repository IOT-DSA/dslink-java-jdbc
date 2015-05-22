package org.dsa.iot.jdbc.provider;

import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.ResultType;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.jdbc.handlers.AddConnectionHandler;
import org.dsa.iot.jdbc.handlers.ConfigureConnectionHandler;
import org.dsa.iot.jdbc.handlers.DeleteConnectionHandler;
import org.dsa.iot.jdbc.handlers.EditConnectionHandler;
import org.dsa.iot.jdbc.handlers.QueryHandler;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;

public class ActionProvider {

	public Action getDeleteConnectionAction(NodeManager manager) {
		Action action = new Action(Permission.READ,
				new DeleteConnectionHandler(manager));
		return action;
	}

	public Action getEditConnectioAction(JdbcConfig config) {
		Action action = new Action(Permission.READ, new EditConnectionHandler(
				config));
		action.addParameter(new Parameter(JdbcConstants.NAME, ValueType.STRING,
				new Value(config.getName())));
		action.addParameter(new Parameter(JdbcConstants.URL, ValueType.STRING,
				new Value(config.getUrl())));
		action.addParameter(new Parameter(JdbcConstants.USER, ValueType.STRING,
				new Value(config.getUser())));
		action.addParameter(new Parameter(JdbcConstants.PASSWORD,
				ValueType.STRING));
		action.addParameter(new Parameter(JdbcConstants.DRIVER,
				ValueType.STRING, new Value(config.getDriverName())));
		return action;
	}

	public Action getAddConnectionAction(NodeManager manager) {
		Action action = new Action(Permission.READ, new AddConnectionHandler(
				manager));
		action.addParameter(new Parameter(JdbcConstants.NAME, ValueType.STRING));
		action.addParameter(new Parameter(JdbcConstants.URL, ValueType.STRING));
		action.addParameter(new Parameter(JdbcConstants.USER, ValueType.STRING));
		action.addParameter(new Parameter(JdbcConstants.PASSWORD,
				ValueType.STRING));
		action.addParameter(new Parameter(JdbcConstants.DRIVER,
				ValueType.STRING));
		return action;
	}

	public Action getConfigureConnectioAction(JdbcConfig config) {
		Action action = new Action(Permission.READ,
				new ConfigureConnectionHandler(config));
		action.addParameter(new Parameter(JdbcConstants.TIMEOUT,
				ValueType.NUMBER, new Value(config.getTimeout())));
		action.addParameter(new Parameter(JdbcConstants.POOLABLE,
				ValueType.BOOL, new Value(config.isPoolable())));
		return action;
	}

	public Action getQueryAction(JdbcConfig config) {
		Action action = new Action(Permission.READ, new QueryHandler(config));
		action.addParameter(new Parameter(JdbcConstants.SQL, ValueType.STRING));
		action.setResultType(ResultType.TABLE);
		return action;
	}
}
