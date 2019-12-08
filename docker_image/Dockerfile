FROM ubuntu:18.04
RUN apt update
RUN apt install -y openjdk-8-jdk
RUN apt install -y mongodb
RUN apt install -y unoconv
COPY . /opt/docdb
RUN service mongodb start &&\
    java -jar /opt/docdb/docdb-1.0-SNAPSHOT-jar-with-dependencies.jar import opt/docdb/export.zip
CMD service mongodb start && java -jar /opt/docdb/docdb-1.0-SNAPSHOT-jar-with-dependencies.jar