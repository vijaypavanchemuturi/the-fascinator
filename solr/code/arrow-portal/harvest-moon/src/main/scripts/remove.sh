#!/bin/sh
URL="http://localhost:8080/solr/fedora?action=deletePid&commit=true&pid=$1"
curl $URL
