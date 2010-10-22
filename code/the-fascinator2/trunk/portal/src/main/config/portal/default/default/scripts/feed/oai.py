import random, time, os
from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfig, JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream

class SolrDoc:
    def __init__(self, json):
        self.json = json

    def getField(self, name):
        return self.json.getList("response/docs/%s" % name).get(0)

    def getFieldText(self, name):
        return self.json.get("response/docs/%s" % name)

    def getFieldList(self, name):
        return self.json.getList("response/docs/%s" % name)

    def getDublinCore(self):
        dc = self.json.getList("response/docs").get(0)
        remove = ["dc_title", "dc_description"]
        for entry in dc:
            if not entry.startswith("dc_"):
                remove.append(entry)
        for key in remove:
            dc.remove(key)
        return JsonConfigHelper(dc).getMap("/")

    def toString(self):
        return self.json.toString()

class OaiPmhError:
    def __init__(self, code, message):
        self.__code = code
        self.__message = message

    def getCode(self):
        return self.__code

    def getMessage(self):
        return self.__message

class OaiPmhVerb:
    def __init__(self, formData):
        self.__error = None
        self.__verb = formData.get("verb")
        print " * verb=%s" % self.__verb
        if self.__verb is None:
            self.__error = OaiPmhError("badVerb", "No verb was specified")
        elif self.__verb in ["GetRecord", "ListIdentifiers", "ListRecords"]:
            self.__metadataPrefix = formData.get("metadataPrefix")
            if self.__metadataPrefix is None:
                #check if resumptionToken exist
                self.__resumptionToken = formData.get("resumptionToken")
                if self.__resumptionToken is None:
                    self.__error = OaiPmhError("badArgument", "Missing required argument: metadataPrefix")
                else:
                    self.__metadataPrefix = self.__resumptionToken.split("/")[3]
            elif self.__metadataPrefix != "oai_dc" and self.__metadataPrefix != "rif_cs":
                self.__error = OaiPmhError("cannotDisseminateFormat",
                                           "Record not available as metadata type: %s" % self.__metadataPrefix)
        elif self.__verb in ["Identify", "ListMetadataFormats", "ListSets"]:
            pass
        else:
            self.__error = OaiPmhError("badVerb", "Unknown verb: '%s'" % self.__verb)

    def getError(self):
        return self.__error

    def getVerb(self):
        return self.__verb

    def getMetadataPrefix(self):
        return self.__metadataPrefix

    def getIdentifier(self):
        return self.__identifier

class ResumptionToken:
    def __init__(self, token=None, start=0, metadataPrefix=""):
        if token is None:
            random.seed()
            token = "%016x" % random.getrandbits(128)
        self.__token = token
        self.__start = start
        self.__metadataPrefix = metadataPrefix
    
    def getToken(self):
        return self.__token
    
    def getStart(self):
        return self.__start
    
    def getConstructedToken(self):
        return "archive/%s/%s/%s" % (self.__start, self.__token, self.__metadataPrefix)

class OaiData:
    def __init__(self):
        pass

    def __activate__(self, context):
        self.velocityContext = context
        self.services = context["Services"]
        self.log = context["log"]
        self.sessionState = context["sessionState"]
        self.portalDir = context["portalDir"]
        self.__result = None
        self.__token = None

        print " * oai.py: formData=%s" % self.vc("formData")
        self.vc("request").setAttribute("Content-Type", "text/xml")
        self.__request = OaiPmhVerb(self.vc("formData"))
        if self.getError() is None and \
                self.getVerb() in ["GetRecord", "ListIdentifiers", "ListRecords"]:
            self.__search()

    # Get from velocity context
    def vc(self, index):
        if self.velocityContext[index] is not None:
            return self.velocityContext[index]
        else:
            self.log.error("ERROR: Requested context entry '" + index + "' doesn't exist")
            return None

    def getVerb(self):
        return self.getRequest().getVerb()

    def getError(self):
        return self.getRequest().getError()

    def getResponseDate(self):
        return time.strftime("%Y-%m-%dT%H:%M:%SZ")

    def getRequest(self):
        return self.__request

    def getResult(self):
        return self.__result

    def getElement(self, elementName, values):
        elementStr = ""
        if values:
            for value in values:
                elementStr += "<%s>%s</%s>" % (elementName, value, elementName)
        return elementStr

    def __search(self):
        self.__result = JsonConfigHelper()

        portal = self.services.getPortalManager().get(self.vc("portalId"))
        recordsPerPage = portal.recordsPerPage

        query = self.vc("formData").get("query")
        if query is None or query == "":
            query = "*:*"
        req = SearchRequest(query)
        req.setParam("facet", "true")
        req.setParam("rows", str(recordsPerPage))
        req.setParam("facet.field", portal.facetFieldList)
        req.setParam("facet.limit", str(portal.facetCount))
        req.setParam("sort", "f_dc_title asc")

        portalQuery = portal.query
        print " * portalQuery=%s" % portalQuery
        if portalQuery:
            req.addParam("fq", portalQuery)

        #Check if there's resumption token exist in the formData
        resumptionToken = self.vc("formData").get("resumptionToken")
        if resumptionToken:
            # sample: archive/145/6f9b63f83bdb5d1e634b0348f77d8cb3/oai_dc
            _, start, token, metadataPrefix = resumptionToken.split("/")
            start = int(start) + recordsPerPage
            self.__token = ResumptionToken(token=token, start=start, metadataPrefix=metadataPrefix)
        else:
            start = recordsPerPage
            metadataPrefix = self.vc("formData").get("metadataPrefix")
            self.__token = ResumptionToken(start=start, metadataPrefix=metadataPrefix)
        
        req.setParam("start", str(start))
        
        resumptionTokenList = self.sessionState.get("resumptionTokenList")
        #resumptionTokenList = {}
        if resumptionTokenList == None:
            resumptionTokenList = {}
        resumptionTokenList[self.__token.getToken()] = self.__token.getConstructedToken()
        
        #Need to know how long the server need to store this token
        self.sessionState.set("resumptionTokenList", resumptionTokenList)
        
        print " * oai.py:", req.toString()

        out = ByteArrayOutputStream()
        self.services.indexer.search(req, out)
        self.__result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
        
    def getToken(self):
        return self.__token

    def getMetadataFormats(self):
        conf = JsonConfig()
        formats = conf.getMap("portal/oai-pmh/metadataFormats")
        return formats


