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
            print " * download.py: URI='%s' OID='%s' PID='%s'" % (uri, object.getId(), payload.getId())
        except StorageException, e:
            payload = None
            print " * download.py : Error {}", e

        if payload is not None:
            filename = os.path.split(payload.getId())[1]
            mimeType = payload.getContentType()
            if mimeType == "application/octet-stream":
                response.setHeader("Content-Disposition", "attachment; filename=%s" % filename)
            out = response.getOutputStream(payload.getContentType())
            IOUtils.copy(payload.open(), out)
            payload.close()
            out.close()
            object.close()
        else:
            response.setStatus(404)
            writer = response.getPrintWriter("text/plain")
            writer.println("Not found: uri='%s'" % uri)
            writer.close()
    
    def __resolve(self, uri):
        slash = uri.rfind("/")
        oid = uri
        pid = uri[slash+1:]
        return self.__get_payload(oid, pid, uri);

    def __get_payload(self, oid, pid, uri):
        try:
            #print " ******* oid=%s\n ******* pid=%s\n" % (oid, pid)
            object = Services.storage.getObject(oid)
            payload = object.getPayload(pid)
        except StorageException, e:
            #oid = uri
            if oid.find("/") > -1:
                slash = oid.rfind("/")
                oid = uri[:slash]
                pid = uri[slash+1:]
                return self.__get_payload(oid, pid, uri)
            else:
                return None, None, None
        return object, payload

scriptObject = DownloadData()