FROM openjdk:8-jdk-alpine
# check http://www.scala-sbt.org/sbt-native-packager/formats/docker.html

ENV SCALA_VERSION="2.11.8"
ENV SBT_VERSION="0.13.13"
ENV SCALA_HOME="/usr/share/scala"
ENV SBT_HOME="/usr/share/sbt"

RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
    apk add --no-cache bash && \
    cd "/tmp" && \
    wget "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
    tar xzf "scala-${SCALA_VERSION}.tgz" && \
    mkdir "${SCALA_HOME}" && \	
    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
    wget "http://dl.bintray.com/sbt/native-packages/sbt/${SBT_VERSION}/sbt-${SBT_VERSION}.tgz" && \
    tar xzf "sbt-${SBT_VERSION}.tgz" && \
    mv "/tmp/sbt-launcher-packaging-${SBT_VERSION}" "${SBT_HOME}" && \
    ln -s "${SBT_HOME}/bin/"* "/usr/bin/" && \
    apk del .build-dependencies && \
    rm -rf "/tmp/"*

# This is for docker to cache the downloaded of jars
RUN sbt sbtVersion
COPY src build.sbt sbt /app/src/
WORKDIR /app/src
RUN sbt package
# rmv src
# check how is Class-path being generated via untar the jar or using sbt packageOptions (¿show?)
# RUN sbt 'show compile:packageBin::packageOptions'
RUN jar xf ./target/scala-2.11/eventuate-cerdt_2.11-1.0.jar
RUN cat ./META-INF/MANIFEST.MF
RUN ls /app/src/target/scala-2.11/
CMD ["java","-jar","./target/scala-2.11/eventuate-cerdt_2.11-1.0.jar"]
