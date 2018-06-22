FROM openjdk:8-jdk-alpine
MAINTAINER Claus Stadler <cstadler@informatik.uni-leipzig.de>

RUN echo "Foobar111"

ARG JAR_FILE
ARG MAIN_CLASS
ARG ARTIFACT_ID
#ARG MAIN_IMAGE

# Build args cannot be used in entrypoint
ENV JAR_FILE=${JAR_FILE}
ENV MAIN_CLASS=${MAIN_CLASS}
ENV ARTIFACT_ID=${ARTIFACT_ID}
ENV MAIN_IMAGE=${MAIN_IMAGE}


RUN echo "Build args: ${JAR_FILE} ${MAIN_CLASS} ${ARTIFACT_ID} ${MAIN_IMAGE}"

WORKDIR /usr/share/${ARTIFACT_ID}/

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/lib lib
# Add the service itself
ADD target/${JAR_FILE} ${JAR_FILE}


#ENTRYPOINT "/usr/bin/java" "-cp" ".:lib/*" "-Dloader.main=${MAIN_CLASS}" "org.springframework.boot.loader.PropertiesLauncher" "${MAIN_IMAGE}"

ENTRYPOINT "/usr/bin/java" "-cp" ".:lib/*" "${MAIN_CLASS}" "org.springframework.boot.loader.PropertiesLauncher" "${MAIN_IMAGE}"
