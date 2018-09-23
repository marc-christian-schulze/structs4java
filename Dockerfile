FROM ubuntu:16.04

RUN apt-get update && \
    apt-get install -y software-properties-common 

RUN add-apt-repository ppa:openjdk-r/ppa 
RUN apt-get update && \
    apt-get install -y openjdk-7-jdk

RUN apt-get update && \
    apt-get install -y \
        openjdk-8-jdk \
        maven \
        git

RUN mkdir /workspace
WORKDIR /workspace
