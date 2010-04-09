import os
from org.apache.commons.io import IOUtils
from java.net import URLDecoder
from au.edu.usq.fascinator.api.storage import StorageException

class DownloadData:
    def __init__(self):
        basePath = portalId + "/" + pageName
        uri = URLDecoder.decode(request.getAttribute("RequestURI"))
        uri = uri[len(basePath)+1:]
        try:
            object, payload = self.__resolve(uri)
            print "URI='%s' OID='%s' PID='%s'" % (uri, object.getId(), payload.getId())
        except StorageException, e:
            payload = None
            print "Failed to get object: %s" % (str(e))
        if payload is not None:
            filename = os.path.split(payload.getId())[1]
            mimeType = payload.getContentType()
            if mimeType == "application/octet-stream":
                response.setHeader("Content-Disposition", "attachment; filename=%s" % filename)
            out = response.getOutputStream(payload.getContentType())
            IOUtils.copy(payload.open(), out)
            payload.close()
            object.close()
            out.close()
        else:
            response.setStatus(404)
            writer = response.getPrintWriter("text/plain; charset=UTF-8")
            writer.println("Not found: uri='%s'" % uri)
            writer.close()
    
    def __resolve(self, uri):
        slash = uri.find("/")
        oid = uri[:slash]
        pid = uri[slash+1:]
        object = Services.storage.getObject(oid)
        payload = object.getPayload(pid)
        return object, payload

scriptObject = DownloadData()
