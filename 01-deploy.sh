#!/bin/bash

# http://strimzi.io/quickstarts/okd/
# https://developers.redhat.com/blog/2018/10/29/how-to-run-kafka-on-openshift-the-enterprise-kubernetes-with-amq-streams/
# https://developers.redhat.com/products/amq/overview/
# https://developers.redhat.com/blog/2018/05/31/introducing-the-kafka-cdi-library/
# https://developers.redhat.com/blog/2018/07/16/smart-meter-streams-kafka-openshift/
# https://developers.redhat.com/blog/2018/10/15/eventflow-event-driven-microservices-on-openshift-part-1/
# https://developers.redhat.com/blog/2018/05/07/announcing-amq-streams-apache-kafka-on-openshift/

export FILE_NAME="install_and_examples_0"
export PROJECT_NAME="amq-demo"
export CLUSTER_NAME="my-cluster"
export TOPIC_NAME="my-topic"

export WORDCOUNT_IN_TOPIC_NAME="wordcount-in-topic"
export WORDCOUNT_OUT_TOPIC_NAME="wordcount-out-topic"

export USERNAME="opentlc-mgr"

#oc adm policy add-cluster-role-to-user cluster-admin ${USERNAME}

curl -L -o ${FILE_NAME}.zip https://access.redhat.com/node/3667151/423/0

unzip ${FILE_NAME}.zip -d ./${FILE_NAME}

oc new-project ${PROJECT_NAME}

sed -i '' 's/namespace: .*/namespace: amq-demo/' ${FILE_NAME}/install/cluster-operator/*RoleBinding*.yaml

oc apply -n ${PROJECT_NAME} -f ${FILE_NAME}/install/cluster-operator
oc apply -n ${PROJECT_NAME} -f ${FILE_NAME}/examples/templates/cluster-operator

cat << EOF | oc create -n ${PROJECT_NAME} -f -
apiVersion: kafka.strimzi.io/v1alpha1
kind: Kafka
metadata: 
 name: ${CLUSTER_NAME}
spec:
 kafka:
   replicas: 3
   listeners:
     external:
       type: route
   storage:
     type: ephemeral
 zookeeper:
   replicas: 3
   storage:
     type: ephemeral
 entityOperator:
   topicOperator: {}
EOF

cat << EOF | oc -n ${PROJECT_NAME} create -f -
apiVersion: kafka.strimzi.io/v1alpha1
kind: KafkaTopic
metadata:
 name: ${TOPIC_NAME}
 labels:
   strimzi.io/cluster: "${CLUSTER_NAME}"
spec:
 partitions: 3
 replicas: 3
EOF

cat << EOF | oc -n ${PROJECT_NAME} create -f -
apiVersion: kafka.strimzi.io/v1alpha1
kind: KafkaTopic
metadata:
 name: ${WORDCOUNT_IN_TOPIC_NAME}
 labels:
   strimzi.io/cluster: "${CLUSTER_NAME}"
spec:
 partitions: 3
 replicas: 3
EOF

cat << EOF | oc -n ${PROJECT_NAME} create -f -
apiVersion: kafka.strimzi.io/v1alpha1
kind: KafkaTopic
metadata:
 name: ${WORDCOUNT_OUT_TOPIC_NAME}
 labels:
   strimzi.io/cluster: "${CLUSTER_NAME}"
spec:
 partitions: 3
 replicas: 3
EOF