import os
from org.apache.commons.io import IOUtils
from java.net import URLDecoder

class DownloadData:
    def __init__(self):
        basePath = portalId + "/" + pageName
        uri = request.getAttribute("RequestURI")
        uri = uri[len(basePath)+1:]
        print " ***", uri
        if uri.find("%2F") != -1:
            slash = uri.find("/")
        else:
            slash = uri.rfind("/")
        oid = URLDecoder.decode(uri[:slash])
        pid = URLDecoder.decode(uri[slash+1:])
        
        print "\n\n", oid, "\n", pid, "\n\n"
        payload = Services.storage.getPayload(oid, pid)
        print "\n\n", payload, "\n\n"
        filename = os.path.split(pid)[1]
        mimeType = payload.contentType
        if mimeType == "application/octet-stream":
            response.setHeader("Content-Disposition", "attachment; filename=%s" % filename)
        out = response.getOutputStream(payload.contentType)
        IOUtils.copy(payload.inputStream, out)
        out.close()

scriptObject = DownloadData()
