#!/bin/sh
curl http://localhost:8080/solr/update -H "Content-Type: text/xml" --data-binary "<commit/>" $*