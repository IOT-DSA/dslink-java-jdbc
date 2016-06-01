package org.dsa.iot.jdbc.driver;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Container for jdbc driver
 *
 * @author pshvets
 */
public class JdbcDriverHolder implements Driver {

    private Driver driver;

    JdbcDriverHolder(Driver driver) {
        this.driver = driver;
    }

    @Override
    public boolean acceptsURL(String u) throws SQLException {
        return this.driver.acceptsURL(u);
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return this.driver.connect(url, info);
    }

    @Override
    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        return this.driver.getPropertyInfo(url, info);
    }

    @Override
    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.driver.getParentLogger();
    }

}
