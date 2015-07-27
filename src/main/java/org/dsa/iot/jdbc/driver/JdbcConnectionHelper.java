package org.dsa.iot.jdbc.driver;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dsa.iot.jdbc.model.JdbcConfig;

public class JdbcConnectionHelper {
	private static String[] cashedDriversName;

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

	public static String[] getRegisteredDrivers() {
		if (cashedDriversName == null) {
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			Set<String> set = new HashSet<String>();
			while (drivers.hasMoreElements()) {
				Driver driver = drivers.nextElement();
				// skip MySQL fabric
				if (!driver.getClass().getName().contains("fabric")) {
					set.add(driver.getClass().getName());
				}
			}
			cashedDriversName = set.toArray(new String[set.size()]);
		}
		return cashedDriversName.clone();
	}
}
