FROM ubuntu:18.04
RUN apt update
RUN apt install -y openjdk-8-jdk
RUN apt install -y mongodb
RUN apt install -y unoconv

######
#не задавать вопросов
ENV DEBIAN_FRONTEND noninteractive.
RUN apt-get upgrade -y
RUN apt-get install -y language-pack-ru
ENV LANGUAGE ru_RU.UTF-8
ENV LANG ru_RU.UTF-8
ENV LC_ALL ru_RU.UTF-8
RUN locale-gen ru_RU.UTF-8 && dpkg-reconfigure locales
######


COPY . /opt/docdb
RUN service mongodb start &&\
    cd /opt/docdb &&\
    java -jar docdb-1.0-SNAPSHOT-jar-with-dependencies.jar import export.zip
CMD service mongodb start && cd /opt/docdb && java -jar docdb-1.0-SNAPSHOT-jar-with-dependencies.jar