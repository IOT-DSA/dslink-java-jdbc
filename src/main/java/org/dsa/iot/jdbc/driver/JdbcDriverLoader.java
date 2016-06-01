package org.dsa.iot.jdbc.driver;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Registers jdbc driver at runtime
 *
 * @author pshvets
 */
public class JdbcDriverLoader {
    public static void registerDriver(final URL url, String className)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, SQLException {

        URLClassLoader loader = AccessController
                .doPrivileged(new PrivilegedAction<URLClassLoader>() {
                    @Override
                    public URLClassLoader run() {
                        return new URLClassLoader(new URL[]{url});
                    }
                });

        Driver driver = (Driver) Class.forName(className, true, loader)
                .newInstance();
        DriverManager.registerDriver(new JdbcDriverHolder(driver));
    }
}
