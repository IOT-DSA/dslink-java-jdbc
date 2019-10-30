# dslink-java-jdbc

A Java DSLink that works with JDBC.

# MS SQL

## Step 1: Install JDBC DSLink

*   Follow steps under "Add a new DSLink" section from [http://wiki.dglogik.com/dglux5_wiki:getting_started:access_dsa_data](http://wiki.dglogik.com/dglux5_wiki:getting_started:access_dsa_data).
*   Select "From Repository" when you right click on **/links** node. In this case select JDBC from the list and install.

## Step 2: Configure correct MS SQL JDBC driver

*   Download MS SQL JDCB from [https://www.microsoft.com/en-US/download/details.aspx?id=11774](https://www.microsoft.com/en-US/download/details.aspx?id=11774).
*   Install MS SQL driver.
*   Copy MS SQL driver jar file (select correct jar version based on JDK). Typical path is `<Microsoft JDBC Driver 6.0 for SQL Server>\sqljdbc_6.0\enu\jre8` (for JDK 8).
*   Copy driver jar file to **dsa-server\dslinks\jdbc\lib** folder.

## Step 3: Restart DSLink

*   Restart DSLink : **sys** -> **links** -> **JDBC** right-click and select restart dslink.

## Step 4: Configure MS JDBC

*   Right click **downstream** -> **jdbc** (Name may changes based on your DSLink name from step 1).
*   Select MS SQL driver from a list.

## Example Connection Strings
`jdbc:sqlserver://[ServerIP or ServerName]:[Port];instance=[instance name];databaseName=[database name]`

## Notes
*   Default port is 1433. If the port is not default, include port in the connection string.
*   Make sure TCP\IP connection is enabled for SQL Server.
*   Make sure SQL server port is not dynamic. 
