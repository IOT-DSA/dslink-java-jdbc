package org.dsa.iot.jdbc.driver;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dsa.iot.jdbc.model.JdbcConfig;

public class JdbcConnectionHelper {

	public static BasicDataSource configureDataSource(JdbcConfig config) {
		BasicDataSource dataSource = new BasicDataSource();
		// dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setDriverClassName(config.getDriverName());
		// dataSource.setUrl(jdbc:mysql://127.0.0.1:3306);
		dataSource.setUrl(config.getUrl());
		dataSource.setUsername(config.getUser());
		dataSource.setPassword(String.valueOf(config.getPassword()));
		dataSource.setInitialSize(3);
		dataSource.setMaxIdle(10);
		dataSource.setMinIdle(1);
		dataSource.setMaxOpenPreparedStatements(20);
		dataSource.setTestWhileIdle(false);
		dataSource.setTestOnBorrow(false);
		dataSource.setTestOnReturn(false);
		dataSource.setTimeBetweenEvictionRunsMillis(1);
		dataSource.setNumTestsPerEvictionRun(50);
		dataSource.setMinEvictableIdleTimeMillis(1800000);
		return dataSource;
	}
}
