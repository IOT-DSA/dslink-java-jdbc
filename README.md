# dslink-java-jdbc

A Java DSLink that works with JDBC.
## Distributions

Distributions can be ran independent of Gradle and anywhere Java is installed.

### Creating a distribution

Run `./gradlew distZip` from the command line. Distributions will be located
in `build/distributions`.

### Running a distribution

Run `./bin/dslink-java-jdbc -b http://localhost:8080/conn` from the command
line. The link will then be running.

## Test running

A local test run requires a broker to be actively running.

Running: <br />
`./gradlew run -Dexec.args="--broker http://localhost:8080/conn"`
