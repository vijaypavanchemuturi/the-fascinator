#!/bin/sh
URL="http://localhost:8080/solr/fedora?action=savePid&commit=true&pid=$1"
curl $URL
