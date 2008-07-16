#!/bin/sh
URL="http://localhost:8080/solr/fedora?action=fromFoxml&commit=true&foxmlPath=$FEDORA_HOME/data/objects"
curl $URL
