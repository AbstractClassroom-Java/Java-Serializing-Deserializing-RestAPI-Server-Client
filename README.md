# REST Demo

## Start the server

```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=io.github.nathanjrussell.restserver.RestServerMain -Dexec.args="8080"
```

## Run the client

If the client is on the same machine as the server:

```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=io.github.nathanjrussell.Main -Dexec.args="http://127.0.0.1:8080"
```

If the client is on a different machine, use the server computer's IP:

```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=io.github.nathanjrussell.Main -Dexec.args="http://<server-ip>:8080"
```
