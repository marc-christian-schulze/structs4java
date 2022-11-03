FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y software-properties-common 

RUN add-apt-repository ppa:openjdk-r/ppa 
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk

RUN apt-get update && \
    apt-get install -y \
        libgtk-3-dev \
        maven \
        git

RUN apt-get install -y xvfb

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

RUN mkdir /workspace
WORKDIR /workspace