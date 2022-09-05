This is the backend for the Golden Agents distributed query system. If the backend and frontend run on the
same server, the port does not have to be exposed to the outside.

The backend is built using Maven, and can be imported using the Maven configuration in most modern IDEs.
The backend depends on the Net-2APL library.

# Build and run with maven
```bash
cd ../../ # Make sure to clone Net2APL outside of this repository
git clone https://bitbucket.org/goldenagents/net2apl.git
cd net2apl
mvn -U clean install

cd ../golden-agents/backend/
mvn -U clean install

java -jar target/*.jar --configuration <path-to-config-file>
```

Where `<path-to-config>` is a TOML file containing the configuration used to instantiate the agents.
An example of this file can be found in `src/main/resources/golden-agents.toml` (which can be
used as the value for this argument as well)


# Build and run with Docker
The backend has been containerized with Docker, and can be build and run as follows:

```bash
sudo docker [--build-arg VERSION=<version>] build -t golden-agents/backend:<version> .
sudo docker run -p <port>:8080 [--env ALLOWED_ORIGINS=<allowed-origins>] golden-agents/backend 
```

Where
* `<port>` should be replaced with the port on which the backend should be exposed on your server
* `<ALLOWED_ORIGINS>` is a string with all hosts that should be allowed by the CORS policy. The hosts are seperated
  by a comma, and should include the schema (e.g. `http://` or `https://`), full host name, and port. The host from which the front-end will be
  accessed should at least be provided. If the frontend is provided through multiple domains or IP addresses, provide all!
* `<VERSION>` can be replaced with a new version name for the Java build inside the Docker container (but don't use this)
