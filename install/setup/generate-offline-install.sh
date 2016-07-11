#!/bin/bash

cd /opt/thinkbig/setup

wget http://ftp.wayne.edu/apache//activemq/5.13.3/apache-activemq-5.13.3-bin.tar.gz -P ./activemq

wget https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/rpm/elasticsearch/2.3.0/elasticsearch-2.3.0.rpm -P ./elasticsearch/

wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u92-b14/jdk-8u92-linux-x64.tar.gz -P ./java

wget http://apache.mesi.com.ar/nifi/0.6.1/nifi-0.6.1-bin.tar.gz -P ./nifi

cp /opt/thinkbig/thinkbig-services/lib/mysql-connector-java-*.jar ./nifi

tar -cvf thinkbig-install.tar *