FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y software-properties-common 

RUN add-apt-repository ppa:openjdk-r/ppa 
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk

RUN apt-get update && \
    apt-get install -y \
        libgtk-3-dev \
        git

RUN apt-get install -y xvfb

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

RUN apt-get install -y wget
RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -P /tmp && \
    tar xf /tmp/apache-maven-*.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.6 /opt/maven

ENV PATH=/opt/maven/bin:$PATH

RUN mkdir /workspace
WORKDIR /workspace