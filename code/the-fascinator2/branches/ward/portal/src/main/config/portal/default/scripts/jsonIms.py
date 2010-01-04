

from java.net import URLDecoder
from org.apache.commons.io import IOUtils
from java.io import BufferedReader
from java.io import InputStreamReader
from java.lang import StringBuilder

import types


class JsonIms(object):
    def __init__(self):
        print "\n-- JsonIms init --"
        self.__mimeType = "text/plain"       # default mimeType
        request.setAttribute("Content-Type", self.__mimeType)
        json = self.__getJson()
        print json
        responseOutput.write(json)

    def __getJson(self):
        data = {}
        basePath = portalId + "/" + pageName
        uri = URLDecoder.decode(request.getAttribute("RequestURI"))
        oid = uri[len(basePath)+1:]
        payload = Services.storage.getPayload(oid, "imsmanifest.xml")
        if payload is not None:
            try:
                from xml.etree import ElementTree as ElementTree
                #xml = ElementTree()
                #IOUtils.copy(payload.inputStream, out)
                sb = StringBuilder()
                inputStream = payload.inputStream
                reader = BufferedReader(InputStreamReader(inputStream,
                            "UTF-8"))
                while True:
                    line=reader.readLine()
                    if line is None:
                        break
                    sb.append(line).append("\n")
                inputStream.close()
                xmlStr =  sb.toString()
                xml = ElementTree.XML(xmlStr)
                ns = xml.tag[:xml.tag.find("}")+1]
                resources = {}
                for res in xml.findall(ns+"resources/"+ns+"resource"):
                    resources[res.attrib.get("identifier")] = res.attrib.get("href")
                #print resources
                organizations = xml.find(ns+"organizations")
                defaultName = organizations.attrib.get("default")
                organizations = organizations.findall(ns+"organization")
                if defaultName:
                    organizations = [o for o in organizations if o.attrib.get("identifier")==defaultName]
                organization = organizations[0]
                title = organization.find(ns+"title").text
                data["title"] = title
                def processItems(parentNode):
                    items = []
                    for item in parentNode.findall(ns+"item"):
                        a = item.attrib
                        isVisible = a.get("isvisible")
                        idr = a.get("identifierref")
                        id = resources.get(idr)
                        iTitle = item.find(ns+"title").text
                        if type(iTitle) is types.UnicodeType:
                            iTitle = iTitle.encode("utf-8")
                        if isVisible and id and id.endswith(".htm"):
                            children = processItems(item)
                            items.append({"attributes":{"id":id}, \
                                    "data":iTitle, "children":children})
                    return items
                #items = []
                #for item in organization.findall(ns+"item"):
                #    a = item.attrib
                #    isVisible = a.get("isvisible")
                #    idr = a.get("identifierref")
                #    id = resources.get(idr)
                #    iTitle = item.find(ns+"title").text
                #    if isVisible and id and id.endswith(".htm"):
                #        items.append({"attributes":{"id":id}, "data":iTitle})
                #data["nodes"] = items
                data["nodes"] = processItems(organization)
            except Exception, e:
                 data["error"] = "Error - %s" % str(e)
                 print data["error"]
        return repr(data)


scriptObject = JsonIms()
