package org.dsa.iot.jdbc.provider;

import org.dsa.iot.dslink.node.NodeManager;
import org.dsa.iot.dslink.node.Permission;
import org.dsa.iot.dslink.node.actions.Action;
import org.dsa.iot.dslink.node.actions.EditorType;
import org.dsa.iot.dslink.node.actions.Parameter;
import org.dsa.iot.dslink.node.actions.ResultType;
import org.dsa.iot.dslink.node.value.Value;
import org.dsa.iot.dslink.node.value.ValueType;
import org.dsa.iot.jdbc.driver.JdbcConnectionHelper;
import org.dsa.iot.jdbc.handlers.AddConnectionHandler;
import org.dsa.iot.jdbc.handlers.DeleteConnectionHandler;
import org.dsa.iot.jdbc.handlers.EditConnectionHandler;
import org.dsa.iot.jdbc.handlers.QueryHandler;
import org.dsa.iot.jdbc.model.JdbcConfig;
import org.dsa.iot.jdbc.model.JdbcConstants;

public class ActionProvider {

	public Action getDeleteConnectionAction(NodeManager manager) {
        return new Action(Permission.READ,
                new DeleteConnectionHandler(manager));
	}

	public Action getEditConnectioAction(JdbcConfig config) {
		Action action = new Action(Permission.READ, new EditConnectionHandler(
				config));
		action.addParameter(new Parameter(JdbcConstants.URL, ValueType.STRING,
				new Value(config.getUrl())).setPlaceHolder("jdbc:mysql://127.0.0.1:3306"));
		action.addParameter(new Parameter(JdbcConstants.USER, ValueType.STRING,
				new Value(config.getUser())));
		action.addParameter(new Parameter(JdbcConstants.PASSWORD,
				ValueType.STRING).setEditorType(EditorType.PASSWORD));
		action.addParameter(new Parameter(JdbcConstants.DRIVER, ValueType
				.makeEnum(JdbcConnectionHelper.getRegisteredDrivers()),
				new Value(config.getDriverName())));
		action.addParameter(new Parameter(JdbcConstants.DEFAULT_TIMEOUT,
				ValueType.NUMBER, new Value(config.getTimeout())));
		action.addParameter(new Parameter(JdbcConstants.POOLABLE,
				ValueType.BOOL, new Value(config.isPoolable())));
		return action;
	}

	public Action getAddConnectionAction(NodeManager manager) {

		Action action = new Action(Permission.READ, new AddConnectionHandler(
				manager));
		action.addParameter(new Parameter(JdbcConstants.NAME, ValueType.STRING));
		action.addParameter(new Parameter(JdbcConstants.URL, ValueType.STRING).setPlaceHolder("jdbc:mysql://127.0.0.1:3306"));
		action.addParameter(new Parameter(JdbcConstants.USER, ValueType.STRING));
		action.addParameter(new Parameter(JdbcConstants.PASSWORD,
				ValueType.STRING).setEditorType(EditorType.PASSWORD));
		{
			String[] drivers = JdbcConnectionHelper.getRegisteredDrivers();
			Value value;
			if (drivers.length > 0) {
				value = new Value(drivers[0]);
			} else {
				value = new Value((String) null);
			}
			action.addParameter(new Parameter(JdbcConstants.DRIVER, ValueType
					.makeEnum(drivers), value));
		}
		action.addParameter(new Parameter(JdbcConstants.DEFAULT_TIMEOUT,
				ValueType.NUMBER));
		action.addParameter(new Parameter(JdbcConstants.POOLABLE,
				ValueType.BOOL, new Value(true)));
		return action;
	}

	public Action getQueryAction(JdbcConfig config) {
		Action action = new Action(Permission.READ, new QueryHandler(config));
		action.addParameter(new Parameter(JdbcConstants.SQL, ValueType.STRING));
		action.setResultType(ResultType.TABLE);
		return action;
	}
}
