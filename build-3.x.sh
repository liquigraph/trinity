#!/bin/bash
versions=(3.0.0 3.0.11 3.1.0 3.1.6 3.2.0 3.2.3)
for v in ${versions[@]}; do
  echo "Neo4j $v"
  if [[ $v == 3.2* ]]; then
  	mvn -q -am -pl embedded-3x clean package -Dneo4j.version=$v -Pwith-neo4j-common
  else
  	mvn -q -am -pl embedded-3x clean package -Dneo4j.version=$v
  fi
done