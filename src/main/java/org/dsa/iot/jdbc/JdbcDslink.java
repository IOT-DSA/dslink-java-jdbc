package org.dsa.iot.jdbc;

import org.dsa.iot.dslink.DSLink;
import org.dsa.iot.dslink.DSLinkFactory;
import org.dsa.iot.dslink.DSLinkHandler;
import org.dsa.iot.jdbc.model.JdbcConstants;
import org.dsa.iot.jdbc.provider.JdbcProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dslink to work with JDBC
 * 
 * @author pshvets
 *
 */
public class JdbcDslink extends DSLinkHandler {

	private static final Logger LOG = LoggerFactory.getLogger(JdbcDslink.class);

	public static void main(String[] args) {
		args = new String[] { "-b", "http://localhost:8080/conn" };
		DSLinkFactory.startResponder(JdbcConstants.DSLINK_NAME, args,
				new JdbcDslink());
	}

	@Override
	public void onResponderConnected(DSLink link) {
		LOG.info("JDBC DSLink started");
		JdbcProvider provider = new JdbcProvider();
		provider.run(link);
	}
}
