# This repo is under development

Consider the following text as personal notes.

## Firing up the backend

Prerequisites:
* Java 11
* Maven

Steps:
1. Clone and install `net2apl`
2. Install back-end
3. Startup back-end with configuration file (`.toml` files to be found in ~/configs)
4. Verify

```bash
$ cd ../../ # Make sure to clone Net2APL outside of this repository
$ git clone https://bitbucket.org/goldenagents/net2apl.git

$ cd net2apl
$ mvn -U clean install

$ cd ../golden-agents/backend/
$ mvn -U clean install

$ java -jar target/*.jar --configuration <path-to-config-file>
```

Here's a more concrete example of starting the backend:
```bash
$ java -jar target/golden-agents-backend-0.0.2-alpha-SNAPSHOT.jar --configuration ../configs/ecartico-onstage.toml
```

You can verify if the backend is up and running by opening up your favorite browser and browse to:
```
http://127.0.0.1:8080/actuator
```
