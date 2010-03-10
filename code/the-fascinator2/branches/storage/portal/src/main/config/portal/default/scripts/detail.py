import os

from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import PayloadType
from au.edu.usq.fascinator.api.storage import StorageException
from au.edu.usq.fascinator.common import JsonConfigHelper

from java.awt import Desktop
from java.io import ByteArrayInputStream, ByteArrayOutputStream, File, StringWriter
from java.net import URLDecoder, URLEncoder
from java.lang import Boolean, String

from org.apache.commons.io import FileUtils, IOUtils
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

import traceback

from org.w3c.tidy import Tidy

class SolrDoc:
    def __init__(self, json):
        self.json = json

    def getField(self, name):
        field = self.json.getList("response/docs/%s" % name)
        if field.isEmpty():
            return None
        return field.get(0)

    def getFieldText(self, name):
        return self.json.get("response/docs/%s" % name)

    def getFieldList(self, name):
        return self.json.getList("response/docs/%s" % name)

    def getDublinCore(self):
        dc = self.json.getList("response/docs").get(0)
        remove = []
        for entry in dc:
            if not entry.startswith("dc_"):
                remove.append(entry)
        for key in remove:
            dc.remove(key)
        return JsonConfigHelper(dc).getMap("/")

    def toString(self):
        return self.json.toString()

class DetailData:
    def __init__(self):
        self.__object = None
        if formData.get("func") == "open-file":
            self.__openFile()
            writer = response.getPrintWriter("text/plain")
            writer.println("{}")
            writer.close()
        else:
            self.__storage = Services.storage
            uri = URLDecoder.decode(request.getAttribute("RequestURI"))
            basePath = portalId + "/" + pageName
            self.__oid = uri[len(basePath)+1:]
            slash = self.__oid.rfind("/")
            self.__pid = self.__oid[slash+1:]
            try:
                self.__object = self.__storage.getObject(self.__oid)
                self.__payload = self.__object.getPayload(self.__pid)
                self.__mimeType = self.__payload.getContentType()
            except StorageException, e:
                self.__mimeType = "application/octet-stream"

            self.__metadata = JsonConfigHelper()
            print " * detail.py: URI='%s' OID='%s' PID='%s' MIME='%s'" % (uri, self.__oid, self.__pid, self.__mimeType)
            self.__search()

    def __search(self):
        req = SearchRequest('id:"%s"' % self.__oid)
        out = ByteArrayOutputStream()
        Services.indexer.search(req, out)
        self.__json = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
        self.__metadata = SolrDoc(self.__json)

    def canOpenFile(self):
        #HACK check if mimetypes match between index and real file
        #dcFormat = self.__json.get("response/docs/dc_format", "")
        #if dcFormat is not None:
        #    dcFormat = dcFormat[1:-1]
        #return dcFormat == self.__mimeType
        f = File(self.getObject().getId())
        return f.exists();

    def containsPid(self, pid):
        return self.getObject().getPayloadIdList().contains(pid);

    def encode(self, url):
        return URLEncoder.encode(url, "UTF-8")

    def isMetadataOnly(self):
        previewPid = self.getPreview(self.getObject().getId())
        if previewPid == "":
            return True
        else:
            return False

    def getFileName(self, path):
        return os.path.split(path)[1]

    def getFilePathWithoutExt(self, path):
        return os.path.splitext(self.getFileName(path))[0]

    def getMimeType(self):
        return self.__mimeType

    def getSolrResponse(self):
        return self.__json

    def formatName(self, name):
        return name[3:4].upper() + name[4:]

    def formatValue(self, value):
        return value

    def isHidden(self, pid):
        if pid.find("_files%2F")>-1:
            return True
        return False

    def getMetadata(self):
        return self.__metadata

    def getObject(self):
        return self.__object

    def getStorageId(self):
        obj = self.getObject()
        return obj.getId()

    def getFileSize(self, path):
        return FileUtils.byteCountToDisplaySize(os.path.getsize(path))

    def hasSlideShow(self):
        pid = self.__pid
        pid = pid[:pid.find(".")] + ".slide.htm"
        if containsPid(pid):
            return pid
        else:
            return False

    def hasFlv(self):
        pid = self.__pid
        pid = pid[:pid.find(".")] + ".flv"
        if containsPid(pid):
            return pid
        else:
            return False

    def getPdfUrl(self):
        pid = os.path.splitext(self.__pid)[0] + ".pdf"
        return "%s/%s" % (self.__oid, pid)

    def getPayLoadUrl(self, pid):
        return "%s/%s" % (self.__oid, pid)

    def hasHtml(self):
        payloadIdList = self.getObject().getPayloadIdList()
        for payloadId in payloadIdList:
            try:
                payload = self.getObject().getPayload(payloadId)
                mimeType = payload.getContentType()
                if mimeType == "text/html" or mimeType == "application/xhtml+xml":
                    return True
            except StorageException, e:
                pass
        return False

    def hasError(self):
        payloadIdList = self.getObject().getPayloadIdList()
        for payloadId in payloadIdList:
            try:
                payload = self.getObject().getPayload(payloadId)
                if str(payload.getType()) == "Error":
                    return True
            except StorageException, e:
                pass
        return False

    def getError(self):
        payloadIdList = self.getObject().getPayloadIdList()
        for payloadId in payloadIdList:
            try:
                payload = self.getObject().getPayload(payloadId)
                if str(payload.getType()) == "Error":
                    return payload
            except StorageException, e:
                pass
        return None

    def getPreview(self, oid):
        payloadIdList = self.getObject().getPayloadIdList()
        for payloadId in payloadIdList:
            try:
                payload = self.getObject().getPayload(payloadId)
                print " * detail.py : Type = '" + str(payload.getType()) + "'"
                if str(payload.getType()) == "Preview":
                    return payload.getId()
            except StorageException, e:
                pass
        return ""

    def getPayloadContent(self):
        mimeType = self.__mimeType
        print " * detail.py: payload content mimeType=%s" % mimeType
        contentStr = ""
        if mimeType == "application/octet-stream":
            dcFormat = self.__json.get("response/docs/dc_format")
            if dcFormat is not None:
                dcFormat = dcFormat[1:-1]
            print dcFormat, mimeType
            if dcFormat != mimeType:
                return "<div><em>(File not found)</em></div>"
            else:
                return "<div><em>(Binary file)</em></div>"
        elif mimeType.startswith("text/"):
            if mimeType == "text/html":
                contentStr = '<iframe class="iframe-preview" src="%s/%s/download/%s"></iframe>' % \
                    (contextPath, portalId, self.__oid)
            else:
                #print " * detail.py: pid=%s payload=%s" % (pid, payload)
                if self.__payload is not None:
                    sw = StringWriter()
                    sw.write("<pre>")
                    IOUtils.copy(self.__payload.open(), sw)
                    self.__payload.close()
                    sw.write("</pre>")
                    sw.flush()
                    contentStr = sw.toString()
        elif mimeType == "application/pdf" or mimeType.find("vnd.ms")>-1 or mimeType.find("vnd.oasis.opendocument.")>-1:
            # get the html version if exist...
            pid = self.getPreview(self.__oid)
            print " * detail.py: pid=%s" % pid
            #contentStr = '<iframe class="iframe-preview" src="%s/%s/download/%s/%s"></iframe>' % \
            #    (contextPath, portalId, self.__oid, pid)
            try:
                payload = self.getObject().getPayload(pid)
                saxReader = SAXReader(Boolean.parseBoolean("false"))
                try:
                    document = saxReader.read(payload.open())
                    payload.close()
                    slideNode = document.selectSingleNode("//*[local-name()='body']")
                    #linkNodes = slideNode.selectNodes("//img")
                    #contentStr = slideNode.asXML();
                    # encode character entities correctly
                    slideNode.setName("div")
                    out = ByteArrayOutputStream()
                    format = OutputFormat.createPrettyPrint()
                    format.setSuppressDeclaration(True)
                    format.setExpandEmptyElements(True)
                    writer = XMLWriter(out, format)
                    writer.write(slideNode)
                    writer.close()
                    contentStr = out.toString("UTF-8")
                except:
                    traceback.print_exc()
                    contentStr = "<p class=\"error\">No preview available</p>"
            except StorageException, e:
                errOut = ""
                payload = self.getError()
                if payload is not None:
                    out = ByteArrayOutputStream()
                    IOUtils.copy(payload.open(), out)
                    payload.close()
                    errOut = '<pre>' + out.toString("UTF-8") + '</pre>'
                contentStr = '<h4 class="error">No preview available</h4><p>Please try re-rendering by using the Re-Harvest action.</p><h5>Details</h5>' + errOut
        elif self.hasError():
            payload = self.getError()
            out = ByteArrayOutputStream()
            IOUtils.copy(payload.open(), out)
            payload.close()
            contentStr = '<h4 class="error">No preview available</h4><p>Please try re-rendering by using the Re-Harvest action.</p><h5>Details</h5><pre>' + out.toString("UTF-8") + '</pre>'
        return contentStr

    def __openFile(self):
        file = formData.get("file")
        print " * detail.py: opening file %s..." % file
        Desktop.getDesktop().open(File(file))

scriptObject = DetailData()
