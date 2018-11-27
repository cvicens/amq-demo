#!/bin/bash

export FILE_NAME="install_and_examples_0"
export PROJECT_NAME="amq-demo"
export CLUSTER_NAME="my-cluster"
export TOPIC_NAME="my-topic"

git clone https://github.com/hguerrero/amq-examples.git

cd amq-examples/camel-kafka-demo/

oc extract secret/${CLUSTER_NAME}-cluster-ca-cert  --keys=ca.crt --to=- > src/main/resources/ca.crt

keytool -import -trustcacerts -alias ca-root -file src/main/resources/ca.crt -keystore src/main/resources/keystore.jks -storepass password -noprompt

export KAFKA_SERVICE_HOST=$(oc -n ${PROJECT_NAME} get routes ${CLUSTER_NAME}-kafka-bootstrap -o=jsonpath='{.status.ingress[0].host}{"\n"}')
export KAFKA_SERVICE_PORT=$(oc -n ${PROJECT_NAME} get routes ${CLUSTER_NAME}-kafka-bootstrap -o=jsonpath='{.spec.port.targetPort}{"\n"}')

echo "${KAFKA_SERVICE_HOST}:${KAFKA_SERVICE_PORT}"

mvn -Drun.jvmArguments="-Dbootstrap.server=`oc get routes ${CLUSTER_NAME}-kafka-bootstrap -o=jsonpath='{.status.ingress[0].host}{"\n"}'`:443" clean package spring-boot:run