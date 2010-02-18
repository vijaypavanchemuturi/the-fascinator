
from au.edu.usq.fascinator.api.storage import PayloadType
from au.edu.usq.fascinator.common.storage.impl import GenericPayload
from au.edu.usq.fascinator.common import JsonConfigHelper

from java.io import BufferedReader, ByteArrayInputStream, InputStreamReader
from java.lang import String, StringBuilder

class AnotarPayload(GenericPayload):
    def __init__(self, payload, obj):
        if payload is None:
            #print "****** NEW PAYLOAD CREATION"
            self.setId(self.generate_id(obj))
            self.setLabel("Anotar")
            self.setContentType("application/json")
        else:
            GenericPayload.__init__(self, payload)
            self.__content = self.set_content(payload)

        self.setType(PayloadType.Annotation)

    def add_json(self, json):
        jsonObj = JsonConfigHelper(json)
        jsonObj.set("id", self.getId())
        #print "**** anotar.py : add_json() : new id : " + self.getId()
        rootUri = jsonObj.get("annotates/rootUri")
        if rootUri is not None:
            myUri = rootUri + "#" + self.getId()
            jsonObj.set("uri", myUri)
            #print "**** anotar.py : add_json() : new uri : " + myUri

        self.__content = jsonObj.toString()
        #print "**** anotar.py : add_json() : completed json : " + self.__content

    def generate_id(self, obj):
        counter = 0
        fileName = "anotar." + str(counter)
        #print "****** TEST NEW ID : " + fileName
        payload = obj.getPayload(fileName)
        while payload is not None:
            counter = counter + 1
            fileName = "anotar." + str(counter)
            #print "****** TEST NEW ID : " + fileName
            payload = obj.getPayload(fileName)
        #print "****** NEW ID SELECTED : " + fileName
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
        action = formData.get("action")
        self.rootUri = formData.get("rootUri")
        annoId = formData.get("id")
        json = formData.get("json")
        #print "**** anotar.py : Start : " + action

        self.obj = Services.storage.getObject(self.rootUri)

        if action == "get":
            # Response is a list of objects (length 1)
            #print "**** anotar.py : GET : " + annoId
            result = self.__get(annoId)
        elif action == "getList":
            # Repsonse is a list of object (nested)
            #print "**** anotar.py : GET_LIST : " + self.rootUri
            result = self.__get_list()
        elif action == "put":
            # Response is an ID
            #print "**** anotar.py : PUT : " + self.rootUri
            result = self.__put(json)
        writer = response.getPrintWriter("text/plain")
        writer.println(result)
        writer.close()

    def __put(self, json):
        #print "**** anotar.py : __put() : start"
        p = self.__create_payload()
        #print "**** anotar.py : __put() : adding jason : " + json
        p.add_json(json);
        Services.storage.addPayload(self.rootUri, p)
        Services.indexer.index(self.rootUri, p.getId())
        #print "**** anotar.py : __put() : ID : " + p.getId()
        return '{"id": "' + p.getId() + '", "ok": true}'

    def __get(self, annoId):
        #print "**** anotar.py : __get() : start"
        p = self.__get_payload(annoId)
        response = p.get_content();
        if response is not None:
            #print "**** anotar.py : __get() : " + response
            response = "[" + response + "]"
        else:
            response = "[]"
        return response

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

    def __get_payload(self, annoId):
        #print "**** anotar.py : __get_payload() : start"
        payload = self.obj.getPayload(annoId);
        if payload is not None:
            #print "**** anotar.py : __get_payload() : found"
            return AnotarPayload(payload, self.obj)
        else:
            return None

    def __get_payloads(self):
        #print "**** anotar.py : __get_payloads() : start : " + self.rootUri
        payloads = self.obj.getPayloadList()
        return_list = []
        for payload in payloads:
            return_list.append(AnotarPayload(payload, self.obj))
        return return_list


scriptObject = AnotarData()
