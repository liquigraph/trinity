#!/bin/bash
set -e

mvn clean package

versions_2x=(2.0.0 2.0.5 2.1.0 2.1.8 2.2.0 2.2.10 2.3.0 2.3.11)
for v in ${versions_2x[@]}; do
  echo "Neo4j $v"
  if [[ $v == 2.2* ]] || [[ $v == 2.3* ]]; then
  	mvn -q -am -pl embedded-2x clean package -Dneo4j.version=$v -Pwith-neo4j-io
  else
  	mvn -q -am -pl embedded-2x clean package -Dneo4j.version=$v
  fi
done

versions_3x=(3.0.0 3.0.11 3.1.0 3.1.6 3.2.0 3.2.3)
for v in ${versions_3x[@]}; do
  echo "Neo4j $v"
  if [[ $v == 3.2* ]]; then
  	mvn -q -am -pl embedded-3x clean package -Dneo4j.version=$v -Pwith-neo4j-common
  else
  	mvn -q -am -pl embedded-3x clean package -Dneo4j.version=$v
  fi
done

