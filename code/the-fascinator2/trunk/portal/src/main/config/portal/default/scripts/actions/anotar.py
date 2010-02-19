from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import PayloadType
from au.edu.usq.fascinator.common.storage.impl import GenericPayload
from au.edu.usq.fascinator.common import JsonConfigHelper

from java.io import BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader
from java.lang import String, StringBuilder

import json

class AnotarPayload(GenericPayload):
    def __init__(self, payload, obj):
        if payload is None:
            self.setId(self.generate_id(obj))
            self.setLabel("Anotar")
            self.setContentType("application/json")
        else:
            GenericPayload.__init__(self, payload)
            self.__content = self.set_content(payload)

        self.setType(PayloadType.Annotation)

    def add_json(self, json):
        print "**** anotar.py : add_json() : adding json : " + json
        jsonObj = JsonConfigHelper(json)
        jsonObj.set("id", self.getId())
        rootUri = jsonObj.get("annotates/rootUri")
        if rootUri is not None:
            baseUrl = "http://%s:%s" % (request.serverName, serverPort)
            myUri = baseUrl + rootUri + "#" + self.getId()
            jsonObj.set("uri", myUri)

        jsonObj.set("schemaVersionUri", "http://www.purl.org/anotar/schema/0.1")

        self.__content = jsonObj.toString(False)
        return self.__content
        print "**** anotar.py : add_json() : completed json : " + self.__content

    def generate_id(self, obj):
        counter = 0
        fileName = "anotar." + str(counter)
        payload = obj.getPayload(fileName)
        while payload is not None:
            counter = counter + 1
            fileName = "anotar." + str(counter)
            payload = obj.getPayload(fileName)
        return fileName

    def get_content(self):
        return self.__content

    def set_content(self, payload):
        sb = StringBuilder()
        line = None
        inStream = payload.getInputStream()
        try:
            reader = BufferedReader(InputStreamReader(inStream, "UTF-8"))
            line = reader.readLine()
            while line is not None:
                sb.append(line).append("\n")
                line = reader.readLine()
        finally:
            inStream.close()

        return sb.toString()

    def get_if_root(self, rootUri):
        myJson = self.get_content()
        jsonObj = JsonConfigHelper(myJson)
        myUri = jsonObj.get("annotates/uri")
        if myUri == rootUri:
            return myJson
        else:
            return None

    def getInputStream(self):
        ourString = String(self.__content)
        return ByteArrayInputStream(ourString.getBytes("UTF-8"))

class AnotarData:
    def __init__(self):
        self.action = formData.get("action")
        self.rootUri = formData.get("rootUri")
        self.json = formData.get("json")
        self.type = formData.get("type")
        #print "**** anotar.py : Action : " + action

        self.obj = Services.storage.getObject(self.rootUri)

        if self.action == "getSolr":
            # Repsonse is a list of object (nested)
            #print "**** anotar.py : GET_LIST : " + self.rootUri
            result = self.__get_list()
        elif self.action == "getList":
            # Repsonse is a list of object (nested)
            #print "**** anotar.py : GET_SOLR : " + self.rootUri
            result = self.__search_solr()
        elif self.action == "put":
            # Response is an ID
            #print "**** anotar.py : PUT : " + self.rootUri
            result = self.__put()
        writer = response.getPrintWriter("text/plain")
        writer.println(result)
        writer.close()

    def __put(self):
        #print "**** anotar.py : __put() : start"
        p = self.__create_payload()
        #print "**** anotar.py : __put() : adding jason : " + json
        json = p.add_json(self.json)
        Services.storage.addPayload(self.rootUri, p)
        Services.indexer.annotate(self.rootUri, p.getId())
        #print "**** anotar.py : __put() : ID : " + p.getId()
        return json

    def __get_list(self):
        topLevel = "["
        #print "**** anotar.py : __get_list() : object id : " + self.rootUri
        payloads = self.__get_payloads()
        #print "**** anotar.py : __get_list() : payload list size : " + str(len(payloads))
        for payload in payloads:
            if payload.getId()[:7] == "anotar.":
                #print "**** anotar.py : __get_list() : testing : " + payload.getId()
                p = payload.get_if_root(self.rootUri)
                if p is not None:
                    #print "**** anotar.py : __get_list() : found : " + p
                    topLevel += p + ","
        return topLevel + "]"

    def __create_payload(self):
        #print "**** anotar.py : __create_payload() : start"
        return AnotarPayload(None, self.obj)

    def __get_payloads(self):
        #print "**** anotar.py : __get_payloads() : start : " + self.rootUri
        payloads = self.obj.getPayloadList()
        return_list = []
        for payload in payloads:
            return_list.append(AnotarPayload(payload, self.obj))
        return return_list

    def __search_solr(self):
        query = "(rootUri:\"" + self.rootUri + "\""
        query += " AND type:\"" + self.type + "\")"

        req = SearchRequest(query)
        req.setParam("facet", "false")
        req.setParam("rows", str(99999))
        req.setParam("sort", "dateCreated asc")
        req.setParam("start", str(0))

        #security_roles = page.authentication.get_roles_list();
        #security_query = 'security_filter:("' + '" OR "'.join(security_roles) + '")'
        #req.addParam("fq", security_query)

        out = ByteArrayOutputStream()
        Services.indexer.annotateSearch(req, out)
        result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))

        # Every annotation for this URI
        result = result.getJsonList("response/docs")
        docs = []
        rootDocs = []
        docsDict = {}
        # Build a dictionary of the annotations
        for doc in result:
            jsonStr = unicode(doc.get("jsonString")).encode("utf-8")
            doc = json.read(jsonStr)
            doc["replies"] = []
            docs.append(doc)
            docsDict[doc["uri"]] = doc
            if doc["annotates"]["uri"]==doc["annotates"]["rootUri"]:
                rootDocs.append(doc)

        # Now process the dictionary
        for doc in docs:
            # If we are NOT a top level annotation
            if doc["annotates"]["uri"]!=doc["annotates"]["rootUri"]:
                # Find what we are annotating
                d = docsDict[doc["annotates"]["uri"]]
                d["replies"].append(doc)    # Add ourselves to its reply list

        return json.write(rootDocs)

scriptObject = AnotarData()