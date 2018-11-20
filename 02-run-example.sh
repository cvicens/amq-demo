#!/bin/bash

export FILE_NAME="install_and_examples_0"
export PROJECT_NAME="amq-demo"
export CLUSTER_NAME="demo-cluster"
export TOPIC_NAME="demo-topic"

git clone https://github.com/hguerrero/amq-examples.git

cd amq-examples/camel-kafka-demo/

oc extract secret/${CLUSTER_NAME}-cluster-ca-cert  --keys=ca.crt --to=- > src/main/resources/ca.crt

keytool -import -trustcacerts -alias root -file src/main/resources/ca.crt -keystore src/main/resources/keystore.jks -storepass password -noprompt

mvn -Drun.jvmArguments="-Dbootstrap.server=`oc get routes ${CLUSTER_NAME}-kafka-bootstrap -o=jsonpath='{.status.ingress[0].host}{"\n"}'`:443" clean package spring-boot:run