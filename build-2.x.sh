#!/bin/bash
versions=(2.0.0 2.0.5 2.1.0 2.1.8 2.2.0 2.2.10 2.3.0 2.3.11)
for v in ${versions[@]}; do
  echo "Neo4j $v"
  if [[ $v == 2.2* ]] || [[ $v == 2.3* ]]; then
  	mvn -q -am -pl embedded-2x clean package -Dneo4j.version=$v -Pwith-neo4j-io
  else
  	mvn -q -am -pl embedded-2x clean package -Dneo4j.version=$v
  fi
done