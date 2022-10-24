# Arguments
ARG USER=goldenagentsspring

#######################
# BUILD NET2APL STAGE
#######################
FROM maven:3-openjdk-15 AS build-net2apl

# Create working directory
RUN mkdir -p /app
WORKDIR /app

# Clone and build Net-2APL
RUN git clone https://bitbucket.org/goldenagents/net2apl.git net-2apl
WORKDIR net-2apl
RUN mvn install 


#######################
# BUILD STAGE
#######################
FROM build-net2apl as build-stage

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn install

#######################
# RUN STAGE
#######################
FROM openjdk:15-alpine

# Dependencies
RUN apk add dumb-init

# Reuse global variables
ARG USER

# Used to specify what agent configuration to boot with
ENV CONFIG_DIR=src/main/resources
ENV CONFIG=golden-agents.toml
ENV ALLOWED_ORIGINS="http://localhost:4200"

# Create new non-root group and user, and switch to user
RUN addgroup -S ${USER} && adduser -S ${USER} -G ${USER}
USER ${USER}:${USER}
WORKDIR /home/${USER}

# Copy build files
COPY --from=build-stage /app/target/golden-agents-backend-*.jar ./golden-agents-backend.jar
COPY ${CONFIG_DIR}/${CONFIG} ./${CONFIG}
COPY src/main/resources/configs ./configs
COPY src/main/resources/golden-agents-datasets ./golden-agents-datasets
COPY Dockerfile ./
COPY pom.xml ./
COPY data ./data

EXPOSE 8080

# Run backend on start of container
#ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD java -jar golden-agents-backend.jar --configuration ${CONFIG}
