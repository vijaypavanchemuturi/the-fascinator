import os

from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import StorageException
from au.edu.usq.fascinator.common import JsonConfigHelper

from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.net import URLDecoder

from org.apache.commons.io import IOUtils

class DownloadPage:
    def __init__(self):
        pass
    
    def __activate__(self, context):
        self.services = context["Services"]
        self.contextPath = context["contextPath"]
        self.pageName = context["pageName"]
        self.portalId = context["portalId"]
        self.request = context["request"]
        self.response = context["response"]
        
        basePath = self.portalId + "/" + self.pageName
        fullUri = URLDecoder.decode(self.request.getAttribute("RequestURI"))
        uri = fullUri[len(basePath)+1:]
        try:
            object, payload = self.__resolve(uri)
            if object == None:
                self.response.sendRedirect(self.contextPath + "/" + fullUri + "/")
                return
            print "URI='%s' OID='%s' PID='%s'" % (uri, object.getId(), payload.getId())
        except StorageException, e:
            payload = None
            print "Failed to get object: %s" % (str(e))

        if payload is not None:
            filename = os.path.split(payload.getId())[1]
            mimeType = payload.getContentType()
            if mimeType == "application/octet-stream":
                self.response.setHeader("Content-Disposition", "attachment; filename=%s" % filename)

            type = payload.getContentType()
            # Enocode textual responses before sending
            if type is not None and type.startswith("text/"):
                out = ByteArrayOutputStream()
                IOUtils.copy(payload.open(), out)
                payload.close()
                writer = self.response.getPrintWriter(type + "; charset=UTF-8")
                writer.println(out.toString("UTF-8"))
                writer.close()
            # Other data can just be streamed out
            else:
                if type is None:
                    # Send as raw data
                    out = self.response.getOutputStream("application/octet-stream")
                else:
                    out = self.response.getOutputStream(type)
                IOUtils.copy(payload.open(), out)
                payload.close()
                object.close()
                out.close()
        else:
            self.response.setStatus(404)
            writer = self.response.getPrintWriter("text/plain; charset=UTF-8")
            writer.println("Resource not found: uri='%s'" % uri)
            writer.close()

    def __resolve(self, uri):
        slash = uri.find("/")
        if slash == -1:
            return None, None
        oid = uri[:slash]
        try:
            object = self.services.getStorage().getObject(oid)
        except StorageException, se:
            # not found check if oid's are mapped differently, use storage_id
            sid = self.__getStorageId(oid)
            object = self.services.getStorage().getObject(sid)
        pid = uri[slash+1:]
        if pid == "":
            pid = object.getSourceId()
        payload = object.getPayload(pid)
        return object, payload

    def __getStorageId(self, oid):
        req = SearchRequest('id:"%s"' % oid)
        req.addParam("fl", "storage_id")
        out = ByteArrayOutputStream()
        self.services.getIndexer().search(req, out)
        json = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
        return json.getList("response/docs").get(0).get("storage_id")
